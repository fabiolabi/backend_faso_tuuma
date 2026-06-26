package bf.annuaire.artisans.device.dto;

import bf.annuaire.artisans.device.entity.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Enregistrement (ou rafraîchissement) du token FCM d'un appareil par le mobile. */
public record RegisterDeviceRequest(
        @NotBlank @Size(max = 512) String fcmToken, @NotNull DevicePlatform platform) {}
