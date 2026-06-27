package bf.annuaire.artisans.metier.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Vue détaillée d'une enseigne et de son agrégat (catégories, services, horaires, réseaux sociaux,
 * galerie). La note globale ({@code ratingAvg}/{@code ratingCount}) porte sur l'enseigne.
 */
public record MetierDetailDto(
        Long id,
        Long ownerUserId,
        String name,
        List<MetierPhoneDto> phones,
        String description,
        String addressDescription,
        AddressDto address,
        String coverUrl,
        BigDecimal gpsLat,
        BigDecimal gpsLng,
        boolean published,
        boolean active,
        BigDecimal ratingAvg,
        Integer ratingCount,
        String aiSummary,
        List<CategoryDto> categories,
        List<ServiceDto> services,
        List<HourlyDto> hours,
        List<SocialMediaDto> socials,
        List<GalleryItemDto> gallery,
        Instant createdAt,
        Instant updatedAt) {}
