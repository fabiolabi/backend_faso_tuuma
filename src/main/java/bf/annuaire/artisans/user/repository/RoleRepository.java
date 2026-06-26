package bf.annuaire.artisans.user.repository;

import bf.annuaire.artisans.user.entity.Role;
import bf.annuaire.artisans.user.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
