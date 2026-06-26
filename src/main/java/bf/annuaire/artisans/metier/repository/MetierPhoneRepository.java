package bf.annuaire.artisans.metier.repository;

import bf.annuaire.artisans.metier.entity.MetierPhone;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetierPhoneRepository extends JpaRepository<MetierPhone, Long> {

    List<MetierPhone> findByMetierIdOrderByIdAsc(Long metierId);

    void deleteByMetierId(Long metierId);
}
