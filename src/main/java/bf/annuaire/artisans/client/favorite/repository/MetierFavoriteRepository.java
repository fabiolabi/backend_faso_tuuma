package bf.annuaire.artisans.client.favorite.repository;

import bf.annuaire.artisans.client.favorite.entity.MetierFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface MetierFavoriteRepository extends JpaRepository<MetierFavorite, Long> {

    boolean existsByClientIdAndMetierId(Long clientId, Long metierId);

    @Modifying
    void deleteByClientIdAndMetierId(Long clientId, Long metierId);

    /** Favoris du client, enseigne (et sa couverture/adresse/catégories) chargée pour le mapping résumé. */
    @EntityGraph(attributePaths = {"metier", "metier.cover", "metier.address", "metier.categories"})
    Page<MetierFavorite> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);
}
