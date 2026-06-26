package bf.annuaire.artisans.device.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.device.dto.DeviceTokenDto;
import bf.annuaire.artisans.device.dto.RegisterDeviceRequest;
import bf.annuaire.artisans.device.service.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tokens FCM des appareils ({@code /api/devices}). Authentification requise : un token est rattaché à
 * l'utilisateur connecté, cible des notifications push. Le mobile (ré)enregistre son token au login /
 * au lancement de l'app, et le désenregistre au logout.
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Appareils", description = "Enregistrement des tokens FCM (notifications push)")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @Operation(summary = "Enregistrer ou rafraîchir le token FCM de l'appareil")
    @PostMapping
    public DeviceTokenDto register(
            @Valid @RequestBody RegisterDeviceRequest request, @AuthenticationPrincipal AuthPrincipal principal) {
        return deviceTokenService.register(principal, request);
    }

    @Operation(summary = "Mes appareils enregistrés")
    @GetMapping
    public List<DeviceTokenDto> mine(@AuthenticationPrincipal AuthPrincipal principal) {
        return deviceTokenService.listMine(principal);
    }

    @Operation(summary = "Désenregistrer le token FCM d'un appareil")
    @DeleteMapping("/{fcmToken}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregister(@PathVariable String fcmToken, @AuthenticationPrincipal AuthPrincipal principal) {
        deviceTokenService.unregister(principal, fcmToken);
    }
}
