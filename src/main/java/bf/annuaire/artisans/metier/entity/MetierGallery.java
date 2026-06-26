package bf.annuaire.artisans.metier.entity;

import bf.annuaire.artisans.media.entity.MediaFile;
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
 * Image de la galerie d'une enseigne (table {@code metier_gallery}). Référence un fichier de la
 * feature {@code media} ; {@code position} ordonne l'affichage. Pas de colonnes d'audit.
 */
@Entity
@Table(name = "metier_gallery")
@Getter
@Setter
@NoArgsConstructor
public class MetierGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metier_id", nullable = false)
    private Metier metier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private MediaFile file;

    @Column(name = "position")
    private Integer position;
}
