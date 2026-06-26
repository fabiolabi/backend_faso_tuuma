package bf.annuaire.artisans.notification.entity;

import bf.annuaire.artisans.common.AbstractAuditingEntity;
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
 * Notification destinée à un utilisateur (table {@code notification}). Persistée pour alimenter un
 * centre de notifications côté mobile (relecture, badge non-lus) en plus de l'envoi push. {@code dataJson}
 * porte la charge utile de navigation (ids cibles). {@code readAt} non nul ⇒ lue. Hérite de
 * {@code createdAt} / {@code updatedAt} (delta-sync mobile) via {@link AbstractAuditingEntity}.
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
public class Notification extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    /** Charge utile de navigation (JSON : type, ids cibles), ou {@code null}. */
    @Column(name = "data_json", columnDefinition = "TEXT")
    private String dataJson;

    @Column(name = "read_at")
    private Instant readAt;
}
