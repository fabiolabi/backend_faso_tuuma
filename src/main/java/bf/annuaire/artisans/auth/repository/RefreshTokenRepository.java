package bf.annuaire.artisans.auth.repository;

import bf.annuaire.artisans.auth.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Révoque en masse tous les refresh tokens encore actifs d'un utilisateur (logout global, reset). */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now "
            + "WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    int revokeAllActiveForUser(@Param("userId") Long userId, @Param("now") Instant now);
}
