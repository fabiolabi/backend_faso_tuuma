package bf.annuaire.artisans.ai.service;

import bf.annuaire.artisans.ai.client.GeminiClient;
import bf.annuaire.artisans.ai.util.Vectors;
import bf.annuaire.artisans.metier.entity.Category;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.entity.Service;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import bf.annuaire.artisans.metier.repository.ServiceRepository;
import java.time.Instant;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calcule et stocke l'embedding sémantique d'une enseigne (nom + description + catégories + noms des
 * prestations) pour la recherche par le sens. Recalculé en asynchrone à chaque changement de contenu.
 * Hors-ligne (IA désactivée), aucun embedding n'est produit ⇒ la recherche retombe sur les mots-clés.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final GeminiClient ai;
    private final MetierRepository metierRepository;
    private final ServiceRepository serviceRepository;

    @Transactional
    public void recompute(Long metierId) {
        if (!ai.isEnabled()) {
            return;
        }
        Metier metier = metierRepository.findWithDetailById(metierId).orElse(null);
        if (metier == null) {
            return;
        }
        float[] vector = ai.embed(buildText(metier));
        if (vector == null) {
            return;
        }
        metier.setSearchEmbedding(Vectors.serialize(vector));
        metier.setEmbeddingUpdatedAt(Instant.now());
        metierRepository.save(metier);
    }

    /** Texte représentatif de l'enseigne pour l'embedding. */
    private String buildText(Metier metier) {
        StringJoiner joiner = new StringJoiner(". ");
        if (metier.getName() != null) {
            joiner.add(metier.getName());
        }
        if (metier.getDescription() != null && !metier.getDescription().isBlank()) {
            joiner.add(metier.getDescription());
        }
        StringJoiner categories = new StringJoiner(", ");
        for (Category category : metier.getCategories()) {
            if (category.getName() != null) {
                categories.add(category.getName());
            }
        }
        if (categories.length() > 0) {
            joiner.add("Catégories : " + categories);
        }
        StringJoiner services = new StringJoiner(", ");
        for (Service service : serviceRepository.findByMetierIdOrderByIdAsc(metier.getId())) {
            if (service.getName() != null) {
                services.add(service.getName());
            }
        }
        if (services.length() > 0) {
            joiner.add("Prestations : " + services);
        }
        return joiner.toString();
    }
}
