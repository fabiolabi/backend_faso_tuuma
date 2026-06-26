package bf.annuaire.artisans.device.entity;

import bf.annuaire.artisans.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Token FCM d'un appareil de l'utilisateur (table {@code device_token}, créée par {@code V1__init}).
 * Un utilisateur peut en avoir plusieurs (multi-appareil). Le token est unique : un même appareil
 * change d'utilisateur après logout/login, le token est alors réattribué. Les tokens invalides
 * (rejetés par FCM) sont purgés à l'envoi. {@code createdAt} est posé à la création (pas d'audit
 * {@code updatedAt} sur cette table).
 */
@Entity
@Table(name = "device_token")
@Getter
@Setter
@NoArgsConstructor
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 10)
    private DevicePlatform platform;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;
}
