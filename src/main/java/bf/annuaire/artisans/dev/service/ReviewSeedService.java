package bf.annuaire.artisans.dev.service;

import bf.annuaire.artisans.ai.client.GeminiClient;
import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.rating.dto.RatingRequest;
import bf.annuaire.artisans.client.rating.service.MetierRatingService;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.dev.dto.SeedRatingsRequest;
import bf.annuaire.artisans.dev.dto.SeedRatingsResponse;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import bf.annuaire.artisans.user.entity.RoleName;
import bf.annuaire.artisans.user.entity.User;
import bf.annuaire.artisans.user.service.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crée des clients fictifs et dépose des avis via le flux standard ({@link MetierRatingService} →
 * événement → modération Gemini asynchrone). Aucun raccourci : les avis passent obligatoirement par
 * l'IA comme en production.
 */
@Service
@RequiredArgsConstructor
public class ReviewSeedService {

    private static final int DEFAULT_TOTAL_REVIEWS = 20;
    private static final int DEFAULT_MAX_PER_METIER = 5;
    private static final int DEFAULT_USER_COUNT = 15;
    private static final String DEFAULT_PASSWORD = "demo1234";
    private static final String DEFAULT_PHONE_PREFIX = "+22677";

    private static final List<String> FIRST_NAMES = List.of(
            "Aminata", "Moussa", "Fatoumata", "Ibrahim", "Mariam", "Ousmane", "Aïcha", "Boubacar", "Salimata",
            "Issa", "Rasmata", "Adama", "Kadidia", "Seydou", "Hawa", "Mahamadi", "Clarisse", "Yacouba", "Bintou",
            "Théophile");

    private static final List<String> LAST_NAMES = List.of(
            "Ouédraogo", "Sawadogo", "Kaboré", "Zongo", "Traoré", "Compaoré", "Nikiéma", "Bado", "Somé", "Ilboudo",
            "Tapsoba", "Barro", "Sanou", "Konaté", "Diallo", "Coulibaly", "Bance", "Pare", "Nana", "Yonli");

    private static final List<String> COMMENTS = List.of(
            "Excellent travail, très professionnel. Je recommande vivement !",
            "Service rapide et soigné, le résultat dépasse mes attentes.",
            "Artisan ponctuel et à l'écoute, je referai appel à lui sans hésiter.",
            "Bon rapport qualité-prix, intervention propre et efficace.",
            "Très satisfait du service, communication claire du début à la fin.",
            "Travail sérieux, je suis content du résultat final.",
            "Prestation de qualité, délais respectés comme convenu.",
            "Artisan compétent et sympathique, je recommande.",
            "Bonne expérience globale, le commerce mérite sa réputation.",
            "Service correct, quelques détails à améliorer mais je suis satisfait.",
            "Intervention soignée, tarif raisonnable pour la qualité fournie.",
            "Très bon contact, travail bien fait et durable.",
            "Je suis ravi du résultat, artisan fiable et professionnel.",
            "Prestation honnête et efficace, je n'hésiterai pas à revenir.",
            "Bon artisan du quartier, service client agréable.");

    private final GeminiClient geminiClient;
    private final MetierRepository metierRepository;
    private final MetierRatingService metierRatingService;
    private final UserService userService;

    @Transactional
    public SeedRatingsResponse seed(SeedRatingsRequest request) {
        if (!geminiClient.isEnabled()) {
            throw new BadRequestException(
                    "Le seed d'avis exige l'IA Gemini : définissez AI_ENABLED=true et GEMINI_API_KEY.");
        }

        List<Metier> metiers = metierRepository.findByPublishedTrueAndActiveTrue();
        if (metiers.isEmpty()) {
            throw new BadRequestException("Aucun commerce publié et actif trouvé.");
        }

        int totalReviews = resolveTotalReviews(request);
        int maxPerMetier = resolveMaxPerMetier(request);
        String password = resolvePassword(request);
        String phonePrefix = resolvePhonePrefix(request);
        int targetUsers = resolveUserCount(request, totalReviews);

        List<User> clients = createClients(targetUsers, password, phonePrefix);
        if (clients.isEmpty()) {
            throw new BadRequestException("Impossible de créer des utilisateurs de seed.");
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int[] reviewsByMetier = new int[metiers.size()];
        Set<String> assignedPairs = new HashSet<>();
        Set<Long> updatedMetierIds = new HashSet<>();
        int reviewsSubmitted = 0;
        int attempts = 0;
        int maxAttempts = totalReviews * 20;

        while (reviewsSubmitted < totalReviews && attempts < maxAttempts) {
            attempts++;
            int metierIndex = random.nextInt(metiers.size());
            if (reviewsByMetier[metierIndex] >= maxPerMetier) {
                continue;
            }

            Metier metier = metiers.get(metierIndex);
            User client = clients.get(random.nextInt(clients.size()));
            Long ownerId = metier.getOwner().getId();

            if (client.getId().equals(ownerId)) {
                continue;
            }

            String pairKey = metier.getId() + ":" + client.getId();
            if (!assignedPairs.add(pairKey)) {
                continue;
            }

            int stars = random.nextInt(3, 6);
            String comment = COMMENTS.get(random.nextInt(COMMENTS.size()));
            AuthPrincipal principal = new AuthPrincipal(client.getId(), client.getPhone(), Set.of(RoleName.CLIENT.name()));

            try {
                metierRatingService.rate(principal, metier.getId(), new RatingRequest(stars, comment));
                reviewsByMetier[metierIndex]++;
                reviewsSubmitted++;
                updatedMetierIds.add(metier.getId());
            } catch (BadRequestException ex) {
                // Paire déjà notée ou autre conflit métier : on essaie une autre combinaison.
            }
        }

        if (reviewsSubmitted == 0) {
            throw new BadRequestException(
                    "Aucun avis déposé : combinaisons épuisées ou commerces/clients insuffisants.");
        }

        return new SeedRatingsResponse(
                clients.size(), reviewsSubmitted, updatedMetierIds.size(), List.copyOf(updatedMetierIds));
    }

    private int resolveTotalReviews(SeedRatingsRequest request) {
        if (request != null && request.totalReviews() != null) {
            return request.totalReviews();
        }
        if (request != null && request.reviewsPerMetier() != null) {
            return Math.max(request.reviewsPerMetier() * 3, DEFAULT_TOTAL_REVIEWS);
        }
        return DEFAULT_TOTAL_REVIEWS;
    }

    private int resolveMaxPerMetier(SeedRatingsRequest request) {
        if (request != null && request.maxReviewsPerMetier() != null) {
            return request.maxReviewsPerMetier();
        }
        if (request != null && request.reviewsPerMetier() != null) {
            return request.reviewsPerMetier();
        }
        return DEFAULT_MAX_PER_METIER;
    }

    private int resolveUserCount(SeedRatingsRequest request, int totalReviews) {
        if (request != null && request.userCount() != null) {
            return request.userCount();
        }
        return Math.max(DEFAULT_USER_COUNT, totalReviews);
    }

    private String resolvePassword(SeedRatingsRequest request) {
        if (request != null && request.password() != null && !request.password().isBlank()) {
            return request.password();
        }
        return DEFAULT_PASSWORD;
    }

    private String resolvePhonePrefix(SeedRatingsRequest request) {
        if (request != null && request.phonePrefix() != null && !request.phonePrefix().isBlank()) {
            return request.phonePrefix();
        }
        return DEFAULT_PHONE_PREFIX;
    }

    private List<User> createClients(int count, String password, String phonePrefix) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int phoneSeq = (int) (System.currentTimeMillis() % 900_000) + 100_000;
        List<User> created = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String phone = phonePrefix + phoneSeq++;
            String firstname = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
            String lastname = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
            String email = "seed." + phone.replace("+", "") + "@fasotuuma.bf";

            try {
                created.add(userService.createUser(firstname, lastname, phone, email, password, RoleName.CLIENT));
            } catch (BadRequestException ex) {
                // Téléphone ou email déjà pris : on tente le suivant.
            }
        }
        return created;
    }
}
