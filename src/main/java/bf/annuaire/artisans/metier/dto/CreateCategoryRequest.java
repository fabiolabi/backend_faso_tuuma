package bf.annuaire.artisans.metier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Création d'une catégorie (ADMIN). {@code parentId} optionnel (catégorie racine si absent). */
public record CreateCategoryRequest(
        @NotBlank @Size(max = 255) String name, @Size(max = 255) String slug, Long parentId) {}
