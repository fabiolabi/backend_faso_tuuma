package bf.annuaire.artisans.metier.mapper;

import bf.annuaire.artisans.metier.dto.CategoryDto;
import bf.annuaire.artisans.metier.entity.Category;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapping {@link Category} → {@link CategoryDto} (MapStruct). Aplatit le parent en {@code parentId}. */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    CategoryDto toDto(Category category);

    List<CategoryDto> toDtoList(Collection<Category> categories);
}
