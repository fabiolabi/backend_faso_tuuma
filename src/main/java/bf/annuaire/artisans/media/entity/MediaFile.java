package bf.annuaire.artisans.media.entity;

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
 * Fichier stocké (table {@code media_file}). Le binaire est écrit sur le système de fichiers local ;
 * {@code storedPath} en contient le chemin <strong>relatif</strong> (généré côté serveur, jamais
 * dérivé du nom client). {@code originalName} n'est conservé que pour l'affichage.
 *
 * <p>N'étend pas {@code AbstractAuditingEntity} : la table ne porte que {@code created_at} (pas
 * d'{@code updated_at}), posé manuellement à la création (même pattern que {@code RefreshToken}).
 */
@Entity
@Table(name = "media_file")
@Getter
@Setter
@NoArgsConstructor
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_path", nullable = false, length = 512)
    private String storedPath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    /** Auteur de l'upload. Nullable au schéma, mais toujours renseigné (upload authentifié). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public MediaFile(
            String originalName,
            String storedPath,
            String contentType,
            long sizeBytes,
            User uploadedBy,
            Instant createdAt) {
        this.originalName = originalName;
        this.storedPath = storedPath;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
    }
}
