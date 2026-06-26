package bf.annuaire.artisans.user.repository;

import bf.annuaire.artisans.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Charge l'utilisateur avec son état civil, ses rôles et ses secrets en une seule requête. */
    @EntityGraph(attributePaths = {"person", "roles", "credential"})
    Optional<User> findByPhone(String phone);

    @EntityGraph(attributePaths = {"person", "roles", "credential"})
    Optional<User> findWithDetailsById(Long id);

    /** Recherche par email de l'état civil (canal de réinitialisation du mot de passe). */
    @EntityGraph(attributePaths = {"person", "roles", "credential"})
    Optional<User> findByPersonEmail(String email);

    boolean existsByPhone(String phone);
}
