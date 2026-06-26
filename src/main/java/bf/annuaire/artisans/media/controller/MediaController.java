package bf.annuaire.artisans.media.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.media.dto.MediaFileDto;
import bf.annuaire.artisans.media.entity.MediaFile;
import bf.annuaire.artisans.media.mapper.MediaFileMapper;
import bf.annuaire.artisans.media.service.MediaService;
import bf.annuaire.artisans.media.service.MediaService.MediaContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stockage générique de fichiers ({@code /api/media}). L'upload et la suppression exigent un JWT ;
 * le téléchargement et les métadonnées sont publics (les photos des enseignes publiées doivent être
 * visibles sans authentification).
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "Media", description = "Upload, téléchargement et suppression de fichiers")
public class MediaController {

    private final MediaService mediaService;
    private final MediaFileMapper mediaFileMapper;

    @Operation(summary = "Upload d'un fichier (image JPEG/PNG/WebP, max 5 Mo)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MediaFileDto upload(
            @RequestParam("file") MultipartFile file, @AuthenticationPrincipal AuthPrincipal principal) {
        MediaFile saved = mediaService.upload(file, principal.userId());
        return mediaFileMapper.toDto(saved);
    }

    @Operation(summary = "Téléchargement du binaire d'un fichier")
    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        MediaContent content = mediaService.loadContent(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.mediaFile().getContentType()))
                .contentLength(content.mediaFile().getSizeBytes())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + content.mediaFile().getOriginalName() + "\"")
                .body(content.resource());
    }

    @Operation(summary = "Métadonnées d'un fichier")
    @GetMapping("/{id}/info")
    public MediaFileDto info(@PathVariable Long id) {
        return mediaFileMapper.toDto(mediaService.getMetadata(id));
    }

    @Operation(summary = "Suppression d'un fichier (auteur ou administrateur)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        mediaService.delete(id, principal);
    }
}
