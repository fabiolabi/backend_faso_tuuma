package bf.annuaire.artisans.ai.config;

import java.time.Duration;
import java.util.concurrent.Executor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

/**
 * Beans de la feature {@code ai} : exécuteur asynchrone dédié (l'analyse IA ne doit jamais bloquer la
 * requête HTTP du dépôt d'avis) et client REST vers l'API Gemini (timeouts courts). Active le support
 * {@code @Async} de l'application.
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    /** Pool dédié aux traitements IA (analyse, agrégation, synthèse, embeddings). */
    @Bean(name = "aiExecutor")
    public Executor aiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-");
        executor.initialize();
        return executor;
    }

    /** Client HTTP vers l'API Gemini (base URL configurable, timeouts courts). */
    @Bean
    public RestClient geminiHttpClient(AiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        return RestClient.builder()
                .baseUrl(properties.getGemini().getBaseUrl())
                .requestFactory(factory)
                .build();
    }
}
