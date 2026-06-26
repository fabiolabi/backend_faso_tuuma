package bf.annuaire.artisans.metier.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lien vers une présence en ligne d'une enseigne (table {@code metier_social_media}). Le partage
 * WhatsApp côté mobile s'appuie notamment sur ces liens (deep link {@code wa.me}). Pas d'audit.
 */
@Entity
@Table(name = "metier_social_media")
@Getter
@Setter
@NoArgsConstructor
public class MetierSocialMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metier_id", nullable = false)
    private Metier metier;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 50)
    private SocialPlatform platform;

    @Column(name = "url", nullable = false, length = 512)
    private String url;
}
