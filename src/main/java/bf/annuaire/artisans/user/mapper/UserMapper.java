package bf.annuaire.artisans.user.mapper;

import bf.annuaire.artisans.user.dto.UserDto;
import bf.annuaire.artisans.user.entity.Role;
import bf.annuaire.artisans.user.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapping {@link User} → {@link UserDto} (MapStruct). Aplatit l'état civil porté par {@code Person}
 * et convertit les rôles en libellés.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "firstname", source = "person.firstname")
    @Mapping(target = "lastname", source = "person.lastname")
    @Mapping(target = "email", source = "person.email")
    @Mapping(target = "city", source = "person.city")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    UserDto toDto(User user);

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream().map(role -> role.getName().name()).collect(Collectors.toSet());
    }
}
