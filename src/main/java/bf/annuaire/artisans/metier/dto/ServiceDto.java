package bf.annuaire.artisans.metier.dto;

/** Prestation d'une enseigne (sortie). Prix en FCFA, optionnels. */
public record ServiceDto(
        Long id, String name, String description, Long priceMin, Long priceMax, boolean active) {}
