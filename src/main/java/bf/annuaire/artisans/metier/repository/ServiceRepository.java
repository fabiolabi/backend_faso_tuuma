package bf.annuaire.artisans.metier.repository;

import bf.annuaire.artisans.metier.entity.Service;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByMetierIdOrderByIdAsc(Long metierId);
}
