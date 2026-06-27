package bf.annuaire.artisans.sms.config;

import bf.annuaire.artisans.sms.sender.AqilasSmsSender;
import bf.annuaire.artisans.sms.sender.NoOpSmsSender;
import bf.annuaire.artisans.sms.sender.SmsSender;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SmsProperties.class)
@Slf4j
public class SmsConfig {

    @Bean
    RestClient aqilasRestClient(SmsProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));
        return RestClient.builder()
                .baseUrl(properties.getAqilas().getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Bean
    SmsSender smsSender(RestClient aqilasRestClient, SmsProperties properties) {
        if (properties.isEnabled()
                && StringUtils.hasText(properties.getAqilas().getApiToken())
                && StringUtils.hasText(properties.getAqilas().getSenderId())) {
            log.info("SMS : émetteur AQILAS (senderId={})", properties.getAqilas().getSenderId());
            return new AqilasSmsSender(aqilasRestClient, properties);
        }
        log.info("SMS : émetteur NoOp (app.sms.enabled=false ou credentials absents).");
        return new NoOpSmsSender();
    }
}
