package bf.annuaire.artisans.device.mapper;

import bf.annuaire.artisans.device.dto.DeviceTokenDto;
import bf.annuaire.artisans.device.entity.DeviceToken;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeviceTokenMapper {

    DeviceTokenDto toDto(DeviceToken token);

    List<DeviceTokenDto> toDtoList(List<DeviceToken> tokens);
}
