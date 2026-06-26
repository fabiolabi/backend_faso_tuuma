package bf.annuaire.artisans.common.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité <strong>stateless</strong> : authentification par JWT transmis dans le
 * header {@code Authorization: Bearer <token>}, jamais par cookie ni session serveur (contrainte
 * mobile). Le filtre JWT et les endpoints d'authentification seront branchés lors de
 * l'implémentation de la feature {@code auth}.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** Endpoints accessibles sans authentification. */
    private static final String[] PUBLIC_GET = {
        "/api/businesses/**", // recherche & consultation des commerces publiés
    };

    private static final String[] PUBLIC_POST = {
        "/api/comments/**", // dépôt d'un commentaire
        "/api/devices/**", // enregistrement d'un device token FCM
    };

    private static final String[] PUBLIC_ANY = {
        "/api/auth/**", // login / refresh / reset password
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/h2-console/**",
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API + JWT par header : pas de CSRF (aucun cookie de session).
                .csrf(csrf -> csrf.disable())
                // Aucune session serveur : chaque requête est authentifiée par son JWT.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET)
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST)
                        .permitAll()
                        .requestMatchers(PUBLIC_ANY)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                // Pas de formulaire de login ni de Basic Auth : 401 propre pour le client mobile.
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                // Requête non authentifiée sur une ressource protégée -> 401 (et non 403/redirection).
                .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint()))
                // Autorise la console H2 (frames) en dev. Sans effet en prod (console désactivée).
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        // TODO: brancher le filtre JWT (validation du Bearer) avant UsernamePasswordAuthenticationFilter
        //       lors de l'implémentation de la feature auth.
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Renvoie un 401 sec (sans corps de challenge HTTP) pour une API consommée en mobile. */
    private AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Non authentifié");
    }
}
