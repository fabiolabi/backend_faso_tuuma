package bf.annuaire.artisans.client.rating.entity;

import bf.annuaire.artisans.common.AbstractAuditingEntity;
import bf.annuaire.artisans.metier.entity.Metier;
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
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Avis d'un client sur une enseigne (table {@code metier_rating}). */
@Entity
@Table(name = "metier_rating")
@Getter
@Setter
@NoArgsConstructor
public class MetierRating extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metier_id", nullable = false)
    private Metier metier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_user_id", nullable = false)
    private User client;

    @Column(name = "star_rating", nullable = false)
    private short starRating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "ai_rating")
    private Short aiRating;

    @Column(name = "sentiment_label", length = 20)
    private String sentimentLabel;

    @Column(name = "sentiment_score", precision = 3, scale = 2)
    private BigDecimal sentimentScore;

    @Column(name = "mismatch", nullable = false)
    private boolean mismatch = false;

    @Column(name = "weight", nullable = false, precision = 3, scale = 2)
    private BigDecimal weight = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RatingStatus status = RatingStatus.PENDING;

    @Column(name = "is_fake", nullable = false)
    private boolean fake = false;

    @Column(name = "is_inappropriate", nullable = false)
    private boolean inappropriate = false;

    @Column(name = "moderation_reason", columnDefinition = "TEXT")
    private String moderationReason;

    @Column(name = "ai_processed_at")
    private Instant aiProcessedAt;
}
