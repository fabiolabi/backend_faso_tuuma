package bf.annuaire.artisans.metier.entity;

import jakarta.persistence.Column;
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
 * Prestation proposée par une enseigne (table {@code service}). Fourchette de prix optionnelle en
 * FCFA ({@code priceMin}/{@code priceMax}). Pas de colonnes d'audit.
 */
@Entity
@Table(name = "service")
@Getter
@Setter
@NoArgsConstructor
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metier_id", nullable = false)
    private Metier metier;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_min")
    private Long priceMin;

    @Column(name = "price_max")
    private Long priceMax;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
