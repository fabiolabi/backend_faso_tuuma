package bf.annuaire.artisans.metier.dto;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Champs communs à la création et à la mise à jour d'une enseigne, pour que {@code MetierService}
 * applique l'écriture des champs de façon uniforme. Implémentée par {@link CreateMetierRequest} et
 * {@link UpdateMetierRequest}.
 */
public interface MetierWriteRequest {
    String name();

    String phone();

    String description();

    String addressDescription();

    AddressDto address();

    BigDecimal gpsLat();

    BigDecimal gpsLng();

    Set<Long> categoryIds();

    Long coverFileId();
}
