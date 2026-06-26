package bf.annuaire.artisans.media.mapper;

import bf.annuaire.artisans.media.dto.MediaFileDto;
import bf.annuaire.artisans.media.entity.MediaFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapping {@link MediaFile} → {@link MediaFileDto} (MapStruct). Calcule l'{@code url} de
 * téléchargement à partir de l'identifiant.
 */
@Mapper(componentModel = "spring")
public interface MediaFileMapper {

    @Mapping(target = "url", expression = "java(\"/api/media/\" + mediaFile.getId())")
    MediaFileDto toDto(MediaFile mediaFile);
}
