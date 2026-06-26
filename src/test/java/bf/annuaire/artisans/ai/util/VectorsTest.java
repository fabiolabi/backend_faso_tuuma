package bf.annuaire.artisans.ai.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Vérifie la (dé)sérialisation et la similarité cosinus de {@link Vectors}. */
class VectorsTest {

    @Test
    void serializeThenParseRoundTrips() {
        float[] vector = {0.1f, -0.25f, 0.5f};
        float[] parsed = Vectors.parse(Vectors.serialize(vector));
        assertThat(parsed).containsExactly(vector, org.assertj.core.data.Offset.offset(1e-6f));
    }

    @Test
    void cosineOfIdenticalVectorsIsOne() {
        float[] v = {1f, 2f, 3f};
        assertThat(Vectors.cosine(v, v)).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void cosineOfOrthogonalVectorsIsZero() {
        assertThat(Vectors.cosine(new float[] {1f, 0f}, new float[] {0f, 1f}))
                .isCloseTo(0.0, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void cosineIsZeroOnNullOrMismatchedLength() {
        assertThat(Vectors.cosine(null, new float[] {1f})).isZero();
        assertThat(Vectors.cosine(new float[] {1f, 2f}, new float[] {1f})).isZero();
    }

    @Test
    void parseHandlesNullAndEmpty() {
        assertThat(Vectors.parse(null)).isNull();
        assertThat(Vectors.parse("[]")).isNull();
    }
}
