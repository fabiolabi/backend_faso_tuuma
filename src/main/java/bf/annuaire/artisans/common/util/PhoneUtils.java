package bf.annuaire.artisans.common.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.util.StringUtils;

/** Normalisation de numéros burkinabè pour SMS (E.164) et recherche en base. */
public final class PhoneUtils {

    private static final String COUNTRY = "226";

    private PhoneUtils() {}

    /** Variantes à tester pour retrouver un utilisateur (exact + formats locaux/internationaux). */
    public static List<String> lookupVariants(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        Set<String> variants = new LinkedHashSet<>();
        String trimmed = raw.trim();
        variants.add(trimmed);

        String digits = trimmed.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) {
            return new ArrayList<>(variants);
        }

        if (digits.startsWith(COUNTRY)) {
            String local = "0" + digits.substring(COUNTRY.length());
            variants.add(local);
            variants.add("+" + digits);
            variants.add(digits);
        } else if (digits.startsWith("0") && digits.length() >= 8) {
            variants.add("+" + COUNTRY + digits.substring(1));
            variants.add(COUNTRY + digits.substring(1));
        } else if (digits.length() == 8) {
            variants.add("0" + digits);
            variants.add("+" + COUNTRY + digits);
            variants.add(COUNTRY + digits);
        }

        return new ArrayList<>(variants);
    }

    /** Format E.164 pour l'API AQILAS, ex. {@code +22670123456}. */
    public static String toE164(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String digits = raw.trim().replaceAll("[^\\d]", "");
        if (digits.isEmpty()) {
            return raw.trim();
        }
        if (digits.startsWith(COUNTRY)) {
            return "+" + digits;
        }
        if (digits.startsWith("0") && digits.length() >= 8) {
            return "+" + COUNTRY + digits.substring(1);
        }
        if (digits.length() == 8) {
            return "+" + COUNTRY + digits;
        }
        return "+" + digits;
    }
}
