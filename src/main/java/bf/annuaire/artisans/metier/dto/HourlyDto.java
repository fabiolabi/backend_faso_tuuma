package bf.annuaire.artisans.metier.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Horaire d'ouverture pour un jour (entrée et sortie). Les heures sont nulles quand l'enseigne est
 * fermée ce jour-là ({@code open = false}).
 */
public record HourlyDto(
        @NotNull DayOfWeek day, LocalTime openHour, LocalTime closeHour, boolean open) {}
