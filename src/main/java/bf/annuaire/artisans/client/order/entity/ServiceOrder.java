package bf.annuaire.artisans.client.order.entity;

import bf.annuaire.artisans.common.AbstractAuditingEntity;
import bf.annuaire.artisans.metier.entity.Metier;
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
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Demande de prestation d'un client à une enseigne (table {@code service_order}). Le
 * {@code service} est optionnel : à {@code null}, c'est une demande libre décrite par
 * {@code message}. Porte {@code createdAt} / {@code updatedAt} via {@link AbstractAuditingEntity}.
 */
@Entity
@Table(name = "service_order")
@Getter
@Setter
@NoArgsConstructor
public class ServiceOrder extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_user_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metier_id", nullable = false)
    private Metier metier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "requested_date")
    private Instant requestedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ServiceOrderStatus status;
}
