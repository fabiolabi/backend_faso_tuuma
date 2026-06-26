package bf.annuaire.artisans.metier.dto;

/** Image de galerie (sortie). {@code url} pointe vers l'endpoint de téléchargement {@code media}. */
public record GalleryItemDto(Long id, Long fileId, String url, Integer position) {}
