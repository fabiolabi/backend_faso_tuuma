package bf.annuaire.artisans.client.profile.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.profile.dto.ClientSummaryDto;
import bf.annuaire.artisans.client.profile.service.ClientProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Espace client ({@code /api/client/summary}) — résumé d'activité de l'utilisateur authentifié. */
@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@Tag(name = "Client — Espace", description = "Résumé d'activité du client")
public class ClientProfileController {

    private final ClientProfileService profileService;

    @Operation(summary = "Résumé de mon activité (demandes, notes, conversations)")
    @GetMapping("/summary")
    public ClientSummaryDto summary(@AuthenticationPrincipal AuthPrincipal principal) {
        return profileService.summary(principal);
    }
}
