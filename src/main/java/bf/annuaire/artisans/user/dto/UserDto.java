package bf.annuaire.artisans.user.dto;

import java.util.Set;

/**
 * Représentation publique légère d'un {@link bf.annuaire.artisans.user.entity.User} (jamais l'entité
 * exposée). Renvoyée notamment dans la réponse d'authentification.
 */
public record UserDto(
        Long id,
        String firstname,
        String lastname,
        String phone,
        String email,
        Set<String> roles) {}
