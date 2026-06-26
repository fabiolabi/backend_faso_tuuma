package bf.annuaire.artisans.user.repository;

import bf.annuaire.artisans.user.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {

    boolean existsByEmail(String email);
}
