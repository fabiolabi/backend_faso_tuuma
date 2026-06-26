package bf.annuaire.artisans.metier.mapper;

import bf.annuaire.artisans.metier.dto.AddressDto;
import bf.annuaire.artisans.metier.dto.GalleryItemDto;
import bf.annuaire.artisans.metier.dto.HourlyDto;
import bf.annuaire.artisans.metier.dto.MetierSummaryDto;
import bf.annuaire.artisans.metier.dto.SocialMediaDto;
import bf.annuaire.artisans.metier.entity.Address;
import bf.annuaire.artisans.metier.entity.Category;
import bf.annuaire.artisans.metier.entity.Hourly;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.entity.MetierGallery;
import bf.annuaire.artisans.metier.entity.MetierSocialMedia;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapping de l'agrégat {@link Metier} vers ses DTO (MapStruct). La vue résumée porte l'URL de
 * couverture, la ville/quartier et les slugs de catégories ; le {@code MetierService} compose la vue
 * détaillée à partir de ces sous-conversions et des collections chargées séparément.
 */
@Mapper(componentModel = "spring")
public interface MetierMapper {

    @Mapping(target = "coverUrl", expression = "java(coverUrl(metier))")
    @Mapping(target = "city", source = "metier.address.city")
    @Mapping(target = "district", source = "metier.address.district")
    @Mapping(target = "categories", source = "metier.categories", qualifiedByName = "categorySlugs")
    @Mapping(target = "distanceKm", source = "distanceKm")
    MetierSummaryDto toSummary(Metier metier, Double distanceKm);

    AddressDto toAddressDto(Address address);

    HourlyDto toHourlyDto(Hourly hourly);

    List<HourlyDto> toHourlyDtoList(List<Hourly> hours);

    SocialMediaDto toSocialDto(MetierSocialMedia socialMedia);

    List<SocialMediaDto> toSocialDtoList(List<MetierSocialMedia> socials);

    @Mapping(target = "fileId", source = "file.id")
    @Mapping(target = "url", expression = "java(\"/api/media/\" + gallery.getFile().getId())")
    GalleryItemDto toGalleryDto(MetierGallery gallery);

    List<GalleryItemDto> toGalleryDtoList(List<MetierGallery> gallery);

    /** URL de la photo de couverture, ou {@code null} si l'enseigne n'en a pas. */
    default String coverUrl(Metier metier) {
        return metier.getCover() != null ? "/api/media/" + metier.getCover().getId() : null;
    }

    @Named("categorySlugs")
    default Set<String> categorySlugs(Set<Category> categories) {
        if (categories == null) {
            return Set.of();
        }
        return categories.stream()
                .map(Category::getSlug)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
