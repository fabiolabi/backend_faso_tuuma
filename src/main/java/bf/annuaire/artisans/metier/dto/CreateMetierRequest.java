package bf.annuaire.artisans.metier.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Création d'une enseigne par un artisan. Elle naît {@code published = false}. {@code address},
 * {@code categoryIds} et {@code coverFileId} sont optionnels et peuvent être complétés plus tard.
 */
public record CreateMetierRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 30) String phone,
        String description,
        String addressDescription,
        @Valid AddressDto address,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal gpsLat,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal gpsLng,
        Set<Long> categoryIds,
        Long coverFileId)
        implements MetierWriteRequest {}
