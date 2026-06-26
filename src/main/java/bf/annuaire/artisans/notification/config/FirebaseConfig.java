package bf.annuaire.artisans.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import bf.annuaire.artisans.notification.sender.FcmPushSender;
import bf.annuaire.artisans.notification.sender.NoOpPushSender;
import bf.annuaire.artisans.notification.sender.PushSender;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

/**
 * Beans de la feature {@code notification} : exécuteur asynchrone dédié (l'envoi push ne doit jamais
 * bloquer la requête HTTP), initialisation conditionnelle de Firebase Admin et choix de l'émetteur
 * push. Le bean {@link FirebaseMessaging} n'est créé que si {@code app.notification.enabled=true} ;
 * sinon {@link #pushSender} retombe sur {@link NoOpPushSender} (repli sans réseau). Le support
 * {@code @Async} est déjà activé par la feature {@code ai}.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(NotificationProperties.class)
public class FirebaseConfig {

    /** Pool dédié à l'envoi des notifications (persistance + appels FCM hors thread HTTP). */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notif-");
        executor.initialize();
        return executor;
    }

    /** FirebaseMessaging réel (prod) : nécessite {@code enabled=true} + un fichier de credentials. */
    @Bean
    @ConditionalOnProperty(prefix = "app.notification", name = "enabled", havingValue = "true")
    public FirebaseMessaging firebaseMessaging(NotificationProperties properties) throws IOException {
        String credentialsFile = properties.getFirebase().getCredentialsFile();
        if (!StringUtils.hasText(credentialsFile)) {
            throw new IllegalStateException(
                    "app.notification.enabled=true mais app.notification.firebase.credentials-file est absent.");
        }
        FirebaseOptions.Builder builder = FirebaseOptions.builder();
        try (InputStream credentials = new FileInputStream(credentialsFile)) {
            builder.setCredentials(GoogleCredentials.fromStream(credentials));
        }
        if (StringUtils.hasText(properties.getFirebase().getProjectId())) {
            builder.setProjectId(properties.getFirebase().getProjectId());
        }
        FirebaseApp app = FirebaseApp.getApps().isEmpty()
                ? FirebaseApp.initializeApp(builder.build())
                : FirebaseApp.getInstance();
        log.info("Firebase Admin initialisé (projet={}).", app.getOptions().getProjectId());
        return FirebaseMessaging.getInstance(app);
    }

    /** Émetteur push : FCM réel si {@link FirebaseMessaging} est présent, sinon repli NoOp. */
    @Bean
    public PushSender pushSender(ObjectProvider<FirebaseMessaging> messaging) {
        FirebaseMessaging fcm = messaging.getIfAvailable();
        if (fcm != null) {
            return new FcmPushSender(fcm);
        }
        log.info("Notifications push : émetteur NoOp (app.notification.enabled=false ou credentials absents).");
        return new NoOpPushSender();
    }
}
