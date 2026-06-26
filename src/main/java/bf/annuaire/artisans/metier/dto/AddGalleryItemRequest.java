package bf.annuaire.artisans.metier.dto;

import jakarta.validation.constraints.NotNull;

/** Ajout d'une image à la galerie : référence un fichier déjà uploadé via la feature {@code media}. */
public record AddGalleryItemRequest(@NotNull Long fileId, Integer position) {}
