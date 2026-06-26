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
 * Numéro de contact d'une enseigne (table {@code metier_phone}). Une enseigne peut en avoir
 * plusieurs ; chacun indique via {@code whatsapp} s'il est joignable sur WhatsApp (le mobile peut
 * alors proposer un deep link {@code wa.me}). Pas d'audit (comme les autres enfants de l'agrégat).
 */
@Entity
@Table(name = "metier_phone")
@Getter
@Setter
@NoArgsConstructor
public class MetierPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metier_id", nullable = false)
    private Metier metier;

    @Column(name = "number", nullable = false, length = 30)
    private String number;

    @Column(name = "is_whatsapp", nullable = false)
    private boolean whatsapp = false;

    @Column(name = "label", length = 60)
    private String label;
}
