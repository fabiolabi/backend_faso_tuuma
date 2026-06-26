package bf.annuaire.artisans.auth.entity;

import bf.annuaire.artisans.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Code de réinitialisation de mot de passe à 6 chiffres (table {@code password_reset_code}, migration
 * V2). Seul le <strong>hash</strong> du code est stocké ({@code codeHash}) ; il expire et n'est
 * utilisable qu'une fois ({@code consumedAt}).
 */
@Entity
@Table(name = "password_reset_code")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PasswordResetCode(User user, String codeHash, Instant expiresAt, Instant createdAt) {
        this.user = user;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    /** Code utilisable = non consommé et non expiré à l'instant donné. */
    public boolean isUsable(Instant now) {
        return consumedAt == null && expiresAt.isAfter(now);
    }
}
