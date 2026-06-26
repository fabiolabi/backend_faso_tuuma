package bf.annuaire.artisans.client.rating.entity;

import bf.annuaire.artisans.metier.entity.Metier;
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
 * Note attribuée par un client à une enseigne (table {@code metier_rating}). Au plus une note par
 * couple (enseigne, client) — contrainte d'unicité {@code uq_metier_rating}.
 *
 * <p>N'étend pas {@code AbstractAuditingEntity} : la table ne porte que {@code created_at} (pas
 * d'{@code updated_at}), posé manuellement à la création (même pattern que {@code MediaFile}).
 */
@Entity
@Table(name = "metier_rating")
@Getter
@Setter
@NoArgsConstructor
public class MetierRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_user_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metier_id", nullable = false)
    private Metier metier;

    @Column(name = "rating", nullable = false)
    private short rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
