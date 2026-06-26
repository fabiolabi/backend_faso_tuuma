package bf.annuaire.artisans.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GeoUtilsTest {

    @Test
    void retourneNullSiUneCoordonneeManque() {
        assertThat(GeoUtils.haversineKm(null, -1.5, 12.3, -1.5)).isNull();
        assertThat(GeoUtils.haversineKm(12.3, null, 12.3, -1.5)).isNull();
        assertThat(GeoUtils.haversineKm(12.3, -1.5, null, -1.5)).isNull();
        assertThat(GeoUtils.haversineKm(12.3, -1.5, 12.3, null)).isNull();
    }

    @Test
    void distanceNulleEntrePointsIdentiques() {
        assertThat(GeoUtils.haversineKm(12.3686, -1.5275, 12.3686, -1.5275)).isEqualTo(0.0);
    }

    @Test
    void distanceConnueOuagaBoboEnviron330km() {
        // Ouagadougou (12.3686, -1.5275) → Bobo-Dioulasso (11.1771, -4.2979) ≈ 330 km.
        Double km = GeoUtils.haversineKm(12.3686, -1.5275, 11.1771, -4.2979);
        assertThat(km).isNotNull();
        assertThat(km).isBetween(315.0, 345.0);
    }
}
