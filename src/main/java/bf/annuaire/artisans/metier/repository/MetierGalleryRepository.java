package bf.annuaire.artisans.metier.repository;

import bf.annuaire.artisans.metier.entity.MetierGallery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetierGalleryRepository extends JpaRepository<MetierGallery, Long> {

    /** Non trié en SQL : {@code position} est un mot réservé (tri en Java par {@code MetierService}). */
    List<MetierGallery> findByMetierId(Long metierId);

    Optional<MetierGallery> findByIdAndMetierId(Long id, Long metierId);
}
