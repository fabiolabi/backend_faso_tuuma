package bf.annuaire.artisans.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Secrets d'authentification d'un {@link User}, isolés dans la table {@code user_credential}
 * (séparation des données sensibles). Contient le hash BCrypt du mot de passe et les compteurs
 * de sécurité (tentatives, verrouillage, dernière connexion).
 */
@Entity
@Table(name = "user_credential")
@Getter
@Setter
@NoArgsConstructor
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone_verified_at")
    private Instant phoneVerifiedAt;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login")
    private Instant lastLogin;

    public UserCredential(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
