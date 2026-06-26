package bf.annuaire.artisans.metier.repository;

import bf.annuaire.artisans.metier.entity.Hourly;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HourlyRepository extends JpaRepository<Hourly, Long> {

    /** Non trié en SQL : {@code day} est un mot réservé (tri en Java par {@code MetierService}). */
    List<Hourly> findByMetierId(Long metierId);

    void deleteByMetierId(Long metierId);
}
