package bf.annuaire.artisans.account.controller;

import bf.annuaire.artisans.auth.dto.AuthResponse;
import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compte utilisateur connecté — actions nécessitant un JWT valide.
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Compte", description = "Actions sur le compte de l'utilisateur connecté")
public class AccountController {

    private final AuthService authService;

    @Operation(
            summary = "Activer le rôle ARTISAN",
            description =
                    "Ajoute le rôle ARTISAN au compte courant (idempotent) et renvoie une nouvelle paire de tokens.")
    @PostMapping("/become-artisan")
    public AuthResponse becomeArtisan(@AuthenticationPrincipal AuthPrincipal principal) {
        return authService.becomeArtisan(principal.userId());
    }
}
