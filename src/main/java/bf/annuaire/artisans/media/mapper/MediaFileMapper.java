package bf.annuaire.artisans.media.mapper;

import bf.annuaire.artisans.media.dto.MediaFileDto;
import bf.annuaire.artisans.media.entity.MediaFile;
import bf.annuaire.artisans.media.service.MediaUrlService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapping {@link MediaFile} → {@link MediaFileDto} (MapStruct). Calcule l'{@code url} de
 * téléchargement à partir de l'identifiant.
 */
@Mapper(componentModel = "spring", uses = MediaUrlService.class)
public interface MediaFileMapper {

    @Mapping(target = "url", source = "mediaFile", qualifiedByName = "mediaUrl")
    MediaFileDto toDto(MediaFile mediaFile);
}
