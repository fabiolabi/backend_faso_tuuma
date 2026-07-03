package bf.annuaire.artisans.dev.controller;

import bf.annuaire.artisans.dev.DevProperties;
import bf.annuaire.artisans.dev.dto.SeedRatingsRequest;
import bf.annuaire.artisans.dev.dto.SeedRatingsResponse;
import bf.annuaire.artisans.dev.service.ReviewSeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Outils de seed pour la démo / les tests (désactivés par défaut). Appeler avec curl une fois
 * {@code SEED_ENABLED=true} et {@code SEED_TOKEN} configurés.
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Tag(name = "Dev / seed", description = "Génération de données de démo (désactivé par défaut)")
public class DevSeedController {

    private final DevProperties devProperties;
    private final ReviewSeedService reviewSeedService;

    @Operation(
            summary = "Crée des clients fictifs et dépose des avis (modération Gemini asynchrone)",
            description =
                    """
                    Nécessite SEED_ENABLED=true, AI_ENABLED=true, GEMINI_API_KEY et le header X-Seed-Token.
                    Les avis passent par le même flux que l'app mobile : dépôt PENDING → analyse Gemini
                    → approbation/rejet → recalcul des notes (quelques secondes en arrière-plan).
                    Exemple curl :
                    curl -X POST http://localhost:8087/api/dev/seed-ratings \\
                      -H "Content-Type: application/json" \\
                      -H "X-Seed-Token: votre-token" \\
                      -d '{"totalReviews":25,"maxReviewsPerMetier":5,"userCount":15,"password":"demo1234"}'
                    """)
    @PostMapping("/seed-ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public SeedRatingsResponse seedRatings(
            @RequestHeader(value = "X-Seed-Token", required = false) String token,
            @Valid @RequestBody(required = false) SeedRatingsRequest request) {
        assertSeedAllowed(token);
        return reviewSeedService.seed(request);
    }

    private void assertSeedAllowed(String token) {
        if (!devProperties.seedEnabled()) {
            throw new AccessDeniedException(
                    "Seed désactivé. Définissez SEED_ENABLED=true dans l'environnement du backend.");
        }
        String expected = devProperties.seedToken();
        if (StringUtils.hasText(expected) && !expected.equals(token)) {
            throw new AccessDeniedException("Token seed invalide (header X-Seed-Token).");
        }
    }
}
