package bf.annuaire.artisans.metier.repository;

import bf.annuaire.artisans.metier.entity.MetierSocialMedia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetierSocialMediaRepository extends JpaRepository<MetierSocialMedia, Long> {

    List<MetierSocialMedia> findByMetierIdOrderByIdAsc(Long metierId);

    void deleteByMetierId(Long metierId);
}
