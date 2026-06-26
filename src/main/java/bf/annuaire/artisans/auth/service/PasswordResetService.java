package bf.annuaire.artisans.auth.service;

import bf.annuaire.artisans.auth.entity.PasswordResetCode;
import bf.annuaire.artisans.auth.repository.PasswordResetCodeRepository;
import bf.annuaire.artisans.auth.security.AuthProperties;
import bf.annuaire.artisans.auth.security.Tokens;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.user.entity.User;
import bf.annuaire.artisans.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Réinitialisation du mot de passe par code à 6 chiffres envoyé par email.
 *
 * <p>La demande ne révèle jamais si l'email existe (anti-énumération) : la réponse est identique
 * qu'un compte corresponde ou non. La confirmation vérifie le code (hashé, non expiré, non consommé),
 * change le mot de passe, consomme le code et révoque les sessions actives.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository resetCodeRepository;
    private final RefreshTokenService refreshTokenService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;

    /** Génère et envoie un code de réinitialisation si un compte correspond à l'email (silencieux sinon). */
    @Transactional
    public void requestReset(String email) {
        Optional<User> maybeUser = userRepository.findByPersonEmail(email);
        if (maybeUser.isEmpty()) {
            log.debug("Demande de reset pour un email inconnu : {}", email);
            return;
        }
        User user = maybeUser.get();
        Instant now = Instant.now();
        String code = Tokens.sixDigitCode();
        PasswordResetCode entity = new PasswordResetCode(
                user,
                Tokens.sha256Hex(code),
                now.plusSeconds(properties.getPasswordReset().getCodeTtlSeconds()),
                now);
        resetCodeRepository.save(entity);
        mailService.sendPasswordResetCode(email, code);
    }

    /**
     * Vérifie le code et change le mot de passe.
     *
     * @throws BadRequestException si le code est invalide, expiré ou déjà utilisé.
     */
    @Transactional
    public void confirmReset(String email, String code, String newPassword) {
        User user = userRepository
                .findByPersonEmail(email)
                .orElseThrow(() -> new BadRequestException("Code de réinitialisation invalide."));

        PasswordResetCode resetCode = resetCodeRepository
                .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .filter(c -> c.isUsable(Instant.now()))
                .filter(c -> c.getCodeHash().equals(Tokens.sha256Hex(code)))
                .orElseThrow(() -> new BadRequestException("Code de réinitialisation invalide ou expiré."));

        user.getCredential().setPasswordHash(passwordEncoder.encode(newPassword));
        resetCode.setConsumedAt(Instant.now());
        refreshTokenService.revokeAllForUser(user.getId());
    }
}
