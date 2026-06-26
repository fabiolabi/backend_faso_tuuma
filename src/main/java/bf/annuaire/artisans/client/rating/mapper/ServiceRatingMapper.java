package bf.annuaire.artisans.client.rating.mapper;

import bf.annuaire.artisans.client.PersonNames;
import bf.annuaire.artisans.client.rating.dto.ServiceRatingDto;
import bf.annuaire.artisans.client.rating.entity.ServiceRating;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapping {@link ServiceRating} → {@link ServiceRatingDto} (MapStruct). */
@Mapper(componentModel = "spring", imports = PersonNames.class)
public interface ServiceRatingMapper {

    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    @Mapping(target = "clientUserId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(PersonNames.fullName(rating.getClient()))")
    ServiceRatingDto toDto(ServiceRating rating);

    List<ServiceRatingDto> toDtoList(List<ServiceRating> ratings);
}
