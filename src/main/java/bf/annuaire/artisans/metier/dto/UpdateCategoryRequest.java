package bf.annuaire.artisans.metier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Mise à jour d'une catégorie (ADMIN). */
public record UpdateCategoryRequest(
        @NotBlank @Size(max = 255) String name, @Size(max = 255) String slug, Long parentId) {}
