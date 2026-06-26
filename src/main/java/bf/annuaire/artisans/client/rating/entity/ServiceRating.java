package bf.annuaire.artisans.client.rating.entity;

import bf.annuaire.artisans.common.AbstractAuditingEntity;
import bf.annuaire.artisans.metier.entity.Service;
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

/**
 * Avis d'un client sur une prestation (table {@code service_rating}). Au plus un avis par couple
 * (service, client) — contrainte d'unicité {@code uq_service_rating}.
 *
 * <p>Le client saisit une note étoilée ({@code starRating}) et un commentaire optionnel. Les champs
 * IA ({@code aiRating}, {@code sentimentLabel}, {@code mismatch}, {@code weight}…) et le {@code status}
 * de modération sont renseignés <strong>en asynchrone</strong> par la feature {@code ai}. La note
 * affichée du service est l'agrégat pondéré des avis {@code APPROVED}.
 */
@Entity
@Table(name = "service_rating")
@Getter
@Setter
@NoArgsConstructor
public class ServiceRating extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_user_id", nullable = false)
    private User client;

    /** Note étoilée saisie par le client (1-5). */
    @Column(name = "star_rating", nullable = false)
    private short starRating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    // ----------------------------------------------------------------- Analyse IA (asynchrone)

    /** Note 1-5 dérivée du texte par l'IA, ou {@code null} tant que non analysé. */
    @Column(name = "ai_rating")
    private Short aiRating;

    @Column(name = "sentiment_label", length = 20)
    private String sentimentLabel;

    @Column(name = "sentiment_score", precision = 3, scale = 2)
    private BigDecimal sentimentScore;

    /** Décalage détecté entre la note étoilée et le sentiment du texte. */
    @Column(name = "mismatch", nullable = false)
    private boolean mismatch = false;

    /** Pondération de l'avis dans l'agrégat (0..1). */
    @Column(name = "weight", nullable = false, precision = 3, scale = 2)
    private BigDecimal weight = BigDecimal.ONE;

    // ----------------------------------------------------------------- Modération

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
