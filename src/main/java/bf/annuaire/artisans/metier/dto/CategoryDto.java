package bf.annuaire.artisans.metier.dto;

/** Représentation publique d'une catégorie. {@code parentId} à {@code null} = catégorie racine. */
public record CategoryDto(Long id, Long parentId, String name, String slug) {}
