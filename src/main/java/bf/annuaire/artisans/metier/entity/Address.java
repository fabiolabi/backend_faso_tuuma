package bf.annuaire.artisans.metier.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Adresse postale d'une enseigne (table {@code address}). Découpée selon les usages de recherche
 * locale au Burkina Faso : ville, quartier, secteur, rue. Plusieurs enseignes peuvent partager une
 * même adresse (N–1). Pas de colonnes d'audit.
 */
@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "sector")
    private String sector;

    @Column(name = "street")
    private String street;
}
