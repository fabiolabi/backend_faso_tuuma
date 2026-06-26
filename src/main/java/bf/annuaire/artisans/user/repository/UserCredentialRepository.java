package bf.annuaire.artisans.user.repository;

import bf.annuaire.artisans.user.entity.UserCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    Optional<UserCredential> findByUserId(Long userId);
}
