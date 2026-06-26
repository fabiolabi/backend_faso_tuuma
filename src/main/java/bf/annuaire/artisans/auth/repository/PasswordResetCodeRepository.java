package bf.annuaire.artisans.auth.repository;

import bf.annuaire.artisans.auth.entity.PasswordResetCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    /** Dernier code émis pour un utilisateur (le plus récent), pour vérification. */
    Optional<PasswordResetCode> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
