package bf.annuaire.artisans.ai.util;

/**
 * Helpers vectoriels pour la recherche sémantique : (dé)sérialisation d'un embedding en JSON compact
 * (tableau de flottants) et similarité cosinus. Volontairement sans dépendance pour rester portable.
 */
public final class Vectors {

    private Vectors() {}

    /** Sérialise un vecteur en tableau JSON, ex. {@code [0.12,-0.34]}. */
    public static String serialize(float[] vector) {
        if (vector == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(vector.length * 8).append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        return sb.append(']').toString();
    }

    /** Parse un vecteur sérialisé par {@link #serialize(float[])} ; {@code null}/vide ⇒ {@code null}. */
    public static float[] parse(String json) {
        if (json == null) {
            return null;
        }
        String body = json.trim();
        if (body.startsWith("[")) {
            body = body.substring(1);
        }
        if (body.endsWith("]")) {
            body = body.substring(0, body.length() - 1);
        }
        body = body.trim();
        if (body.isEmpty()) {
            return null;
        }
        String[] parts = body.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }
        return vector;
    }

    /** Similarité cosinus ∈ [-1,1] ; {@code 0} si l'un des vecteurs est nul/vide ou de norme nulle. */
    public static double cosine(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0 || a.length != b.length) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
