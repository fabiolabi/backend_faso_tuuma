package bf.annuaire.artisans.client.favorite.entity;

import bf.annuaire.artisans.common.AbstractAuditingEntity;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Enseigne mise en favori par un utilisateur (table {@code user_favorite_metier}). Au plus une ligne
 * par couple (client, enseigne) — contrainte d'unicité {@code uq_favorite}. Hérite de
 * {@code createdAt} / {@code updatedAt} (delta-sync mobile) via {@link AbstractAuditingEntity}.
 */
@Entity
@Table(name = "user_favorite_metier")
@Getter
@Setter
@NoArgsConstructor
public class MetierFavorite extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_user_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metier_id", nullable = false)
    private Metier metier;
}
