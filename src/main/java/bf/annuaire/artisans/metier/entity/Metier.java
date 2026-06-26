package bf.annuaire.artisans.metier.entity;

import bf.annuaire.artisans.common.AbstractAuditingEntity;
import bf.annuaire.artisans.media.entity.MediaFile;
import bf.annuaire.artisans.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Enseigne / commerce d'un artisan (table {@code metier}) — entité centrale de l'annuaire.
 *
 * <p>Appartient à un {@link User} ({@code owner}, rôle ARTISAN). Reste {@code published = false} à la
 * création : invisible en recherche tant que le propriétaire ne l'a pas publiée explicitement. La
 * notation est désormais portée par les <strong>services</strong> (l'enseigne n'a plus de note
 * propre) ; {@code searchEmbedding} est le vecteur sémantique (JSON) alimenté par la feature
 * {@code ai} pour la recherche par le sens. Hérite de {@code createdAt} / {@code updatedAt}
 * (delta-sync mobile) via {@link AbstractAuditingEntity}.
 */
@Entity
@Table(name = "metier")
@Getter
@Setter
@NoArgsConstructor
public class Metier extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone", length = 30)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(name = "address_description", columnDefinition = "TEXT")
    private String addressDescription;

    /** Photo de couverture (1–1). Référence un fichier de la feature {@code media}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_file_id")
    private MediaFile cover;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "gps_lat", precision = 9, scale = 6)
    private BigDecimal gpsLat;

    @Column(name = "gps_lng", precision = 9, scale = 6)
    private BigDecimal gpsLng;

    /** Vecteur d'embedding sérialisé en JSON (recherche sémantique, feature {@code ai}). */
    @Column(name = "search_embedding", columnDefinition = "TEXT")
    private String searchEmbedding;

    @Column(name = "embedding_updated_at")
    private java.time.Instant embeddingUpdatedAt;

    @Column(name = "is_published", nullable = false)
    private boolean published = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "metier_category",
            joinColumns = @JoinColumn(name = "metier_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();
}
