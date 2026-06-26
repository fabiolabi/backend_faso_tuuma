package bf.annuaire.artisans.metier.mapper;

import bf.annuaire.artisans.metier.dto.ServiceDto;
import bf.annuaire.artisans.metier.entity.Service;
import java.util.List;
import org.mapstruct.Mapper;

/** Mapping {@link Service} → {@link ServiceDto} (MapStruct). */
@Mapper(componentModel = "spring")
public interface ServiceMapper {

    ServiceDto toDto(Service service);

    List<ServiceDto> toDtoList(List<Service> services);
}
