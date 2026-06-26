package bf.annuaire.artisans.metier.dto;

import bf.annuaire.artisans.metier.entity.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Lien vers une présence en ligne d'une enseigne (entrée et sortie). */
public record SocialMediaDto(
        @NotNull SocialPlatform platform, @NotBlank @Size(max = 512) String url) {}
