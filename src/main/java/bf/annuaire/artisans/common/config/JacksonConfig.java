package bf.annuaire.artisans.common.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.TimeZone;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sérialisation Jackson des dates en UTC ISO-8601 (jamais en timestamp numérique ni en heure
 * locale serveur). Garantit des dates cohérentes pour le client mobile, quel que soit le fuseau
 * du serveur.
 */
@Configuration
public class JacksonConfig {

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jacksonUtcIso8601Customizer() {
        return builder -> {
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.timeZone(TimeZone.getTimeZone("UTC"));
        };
    }
}
