package bf.annuaire.artisans.client.order.mapper;

import bf.annuaire.artisans.client.PersonNames;
import bf.annuaire.artisans.client.order.dto.ServiceOrderDto;
import bf.annuaire.artisans.client.order.entity.ServiceOrder;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapping {@link ServiceOrder} → {@link ServiceOrderDto} (MapStruct). */
@Mapper(componentModel = "spring", imports = PersonNames.class)
public interface ServiceOrderMapper {

    @Mapping(target = "metierId", source = "metier.id")
    @Mapping(target = "metierName", source = "metier.name")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    @Mapping(target = "clientUserId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(PersonNames.fullName(order.getClient()))")
    ServiceOrderDto toDto(ServiceOrder order);

    List<ServiceOrderDto> toDtoList(List<ServiceOrder> orders);
}
