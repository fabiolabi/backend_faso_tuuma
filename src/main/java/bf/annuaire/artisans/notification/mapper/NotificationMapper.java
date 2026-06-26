package bf.annuaire.artisans.notification.mapper;

import bf.annuaire.artisans.notification.dto.NotificationDto;
import bf.annuaire.artisans.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "type", expression = "java(notification.getType().name())")
    @Mapping(target = "read", expression = "java(notification.getReadAt() != null)")
    NotificationDto toDto(Notification notification);
}
