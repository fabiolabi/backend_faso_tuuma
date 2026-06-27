package bf.annuaire.artisans.client.rating.mapper;

import bf.annuaire.artisans.client.PersonNames;
import bf.annuaire.artisans.client.rating.dto.MetierRatingDto;
import bf.annuaire.artisans.client.rating.entity.MetierRating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = PersonNames.class)
public interface MetierRatingMapper {

    @Mapping(target = "metierId", source = "metier.id")
    @Mapping(target = "metierName", source = "metier.name")
    @Mapping(target = "clientUserId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(PersonNames.fullName(rating.getClient()))")
    MetierRatingDto toDto(MetierRating rating);
}
