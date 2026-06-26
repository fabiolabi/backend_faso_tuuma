package bf.annuaire.artisans.notification.dto;

import java.time.Instant;

/** Vue d'une notification pour le mobile (jamais l'entité exposée). {@code read} = déjà lue. */
public record NotificationDto(
        Long id, String type, String title, String body, String dataJson, boolean read, Instant createdAt) {}
