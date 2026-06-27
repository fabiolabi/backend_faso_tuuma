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
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Réinitialisation du mot de passe par code OTP à 6 chiffres (SMS ou email).
 *
 * <p>La demande ne révèle jamais si l'identifiant existe (anti-énumération). La confirmation vérifie
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
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;

    /** Génère et envoie un OTP si un compte correspond à l'identifiant (silencieux sinon). */
    @Transactional
    public void requestReset(String phone, String email) {
        ResetChannel channel = resolveChannel(phone, email);
        Optional<User> maybeUser = findUser(channel, phone, email);
        if (maybeUser.isEmpty()) {
            log.debug(
                    "Demande de reset pour un identifiant inconnu ({}) : {}",
                    channel,
                    channel == ResetChannel.PHONE ? phone : email);
            return;
        }
        User user = maybeUser.get();
        Instant now = Instant.now();
        long ttlSeconds = properties.getPasswordReset().getCodeTtlSeconds();
        String code = Tokens.sixDigitCode();
        PasswordResetCode entity = new PasswordResetCode(
                user, Tokens.sha256Hex(code), now.plusSeconds(ttlSeconds), now);
        resetCodeRepository.save(entity);
        try {
            if (channel == ResetChannel.PHONE) {
                smsOtpService.sendPasswordResetCode(user.getPhone(), code, ttlSeconds);
                log.info("OTP reset envoyé par SMS → userId={}", user.getId());
            } else {
                String to = user.getPerson().getEmail();
                if (!StringUtils.hasText(to)) {
                    log.debug("Reset email demandé mais aucun email sur le compte userId={}", user.getId());
                    return;
                }
                mailService.sendPasswordResetCode(to.trim(), code, ttlSeconds);
                log.info("OTP reset envoyé par email → userId={}", user.getId());
            }
        } catch (RuntimeException e) {
            log.error("Échec envoi OTP reset → userId={} : {}", user.getId(), e.getMessage());
            String detail = e.getMessage();
            if (detail != null && !detail.isBlank()) {
                if (channel == ResetChannel.PHONE
                        && !detail.equals("Envoi SMS impossible.")
                        && !detail.startsWith("Impossible d'envoyer")) {
                    throw new BadRequestException(detail);
                }
                if (channel == ResetChannel.EMAIL
                        && !detail.equals("Envoi email impossible.")
                        && !detail.startsWith("Impossible d'envoyer")) {
                    throw new BadRequestException(detail);
                }
            }
            throw new BadRequestException(
                    channel == ResetChannel.PHONE
                            ? "Impossible d'envoyer le SMS. Réessayez plus tard."
                            : "Impossible d'envoyer l'email. Réessayez plus tard.");
        }
    }

    /**
     * Vérifie le code OTP et change le mot de passe.
     *
     * @throws BadRequestException si le code est invalide, expiré ou déjà utilisé.
     */
    @Transactional
    public void confirmReset(String phone, String email, String code, String newPassword) {
        User user = findUser(resolveChannel(phone, email), phone, email)
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

    private enum ResetChannel {
        PHONE,
        EMAIL
    }

    private ResetChannel resolveChannel(String phone, String email) {
        boolean hasPhone = StringUtils.hasText(phone);
        boolean hasEmail = StringUtils.hasText(email);
        if (hasPhone == hasEmail) {
            throw new BadRequestException("Indiquez un téléphone ou une adresse email.");
        }
        return hasPhone ? ResetChannel.PHONE : ResetChannel.EMAIL;
    }

    private Optional<User> findUser(ResetChannel channel, String phone, String email) {
        return channel == ResetChannel.PHONE ? findUserByPhone(phone) : findUserByEmail(email);
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

    private Optional<User> findUserByEmail(String raw) {
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByPersonEmail(normalized);
    }
}
