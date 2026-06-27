package bf.annuaire.artisans.auth.service;

import bf.annuaire.artisans.auth.entity.PasswordResetCode;
import bf.annuaire.artisans.auth.repository.PasswordResetCodeRepository;
import bf.annuaire.artisans.auth.security.AuthProperties;
import bf.annuaire.artisans.auth.security.Tokens;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.common.util.PhoneUtils;
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
 * Réinitialisation du mot de passe par code OTP à 6 chiffres envoyé par SMS (AQILAS).
 *
 * <p>La demande ne révèle jamais si le téléphone existe (anti-énumération). La confirmation vérifie
 * le code (hashé, non expiré, non consommé), change le mot de passe et révoque les sessions actives.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository resetCodeRepository;
    private final RefreshTokenService refreshTokenService;
    private final SmsOtpService smsOtpService;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;

    /** Génère et envoie un OTP si un compte correspond au téléphone (silencieux sinon). */
    @Transactional
    public void requestReset(String phone) {
        Optional<User> maybeUser = findUserByPhone(phone);
        if (maybeUser.isEmpty()) {
            log.debug("Demande de reset pour un téléphone inconnu : {}", phone);
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
        try {
            smsOtpService.sendPasswordResetCode(
                    user.getPhone(), code, properties.getPasswordReset().getCodeTtlSeconds());
            log.info("OTP reset envoyé par SMS → userId={}", user.getId());
        } catch (RuntimeException e) {
            log.error("Échec envoi OTP reset → userId={} : {}", user.getId(), e.getMessage());
            String detail = e.getMessage();
            if (detail != null && !detail.isBlank() && !detail.equals("Envoi SMS impossible.")) {
                throw new BadRequestException(detail);
            }
            throw new BadRequestException("Impossible d'envoyer le SMS. Réessayez plus tard.");
        }
    }

    /**
     * Vérifie le code OTP et change le mot de passe.
     *
     * @throws BadRequestException si le code est invalide, expiré ou déjà utilisé.
     */
    @Transactional
    public void confirmReset(String phone, String code, String newPassword) {
        User user = findUserByPhone(phone)
                .orElseThrow(() -> new BadRequestException("Code de réinitialisation invalide."));

        PasswordResetCode resetCode = resetCodeRepository
                .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .filter(c -> c.isUsable(Instant.now()))
                .filter(c -> c.getCodeHash().equals(Tokens.sha256Hex(code)))
                .orElseThrow(() -> new BadRequestException("Code de réinitialisation invalide ou expiré."));

        user.getCredential().setPasswordHash(passwordEncoder.encode(newPassword));
        resetCode.setConsumedAt(Instant.now());
        refreshTokenService.revokeAllForUser(user.getId());
        log.info("Mot de passe réinitialisé → userId={}", user.getId());
    }

    private Optional<User> findUserByPhone(String raw) {
        for (String variant : PhoneUtils.lookupVariants(raw)) {
            Optional<User> user = userRepository.findByPhone(variant);
            if (user.isPresent()) {
                return user;
            }
        }
        return Optional.empty();
    }
}
