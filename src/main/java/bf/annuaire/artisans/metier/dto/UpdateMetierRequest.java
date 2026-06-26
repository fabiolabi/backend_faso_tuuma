package bf.annuaire.artisans.metier.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Mise à jour d'une enseigne. Remplace l'ensemble des champs fournis ; {@code address},
 * {@code categoryIds} et {@code coverFileId} à {@code null} laissent la valeur existante inchangée
 * (voir {@code MetierService}).
 */
public record UpdateMetierRequest(
        @NotBlank @Size(max = 255) String name,
        String description,
        String addressDescription,
        @Valid AddressDto address,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal gpsLat,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal gpsLng,
        Set<Long> categoryIds,
        Long coverFileId)
        implements MetierWriteRequest {}
