package bf.annuaire.artisans.metier.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Vue détaillée d'une enseigne et de son agrégat (catégories, services, horaires, réseaux sociaux,
 * galerie). {@code ratingAvg} / {@code ratingCount} sont en lecture seule (alimentés par la feature
 * {@code comment}).
 */
public record MetierDetailDto(
        Long id,
        Long ownerUserId,
        String name,
        String phone,
        String description,
        String addressDescription,
        AddressDto address,
        String coverUrl,
        BigDecimal gpsLat,
        BigDecimal gpsLng,
        BigDecimal ratingAvg,
        Integer ratingCount,
        boolean published,
        boolean active,
        List<CategoryDto> categories,
        List<ServiceDto> services,
        List<HourlyDto> hours,
        List<SocialMediaDto> socials,
        List<GalleryItemDto> gallery,
        Instant createdAt,
        Instant updatedAt) {}
