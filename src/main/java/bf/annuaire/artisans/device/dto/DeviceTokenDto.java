package bf.annuaire.artisans.device.dto;

import bf.annuaire.artisans.device.entity.DevicePlatform;
import java.time.Instant;

/** Vue d'un token d'appareil enregistré (jamais l'entité exposée). */
public record DeviceTokenDto(
        Long id, String fcmToken, DevicePlatform platform, Instant lastSeenAt, Instant createdAt) {}
