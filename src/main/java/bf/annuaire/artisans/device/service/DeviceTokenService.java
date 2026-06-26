package bf.annuaire.artisans.device.service;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.device.dto.DeviceTokenDto;
import bf.annuaire.artisans.device.dto.RegisterDeviceRequest;
import bf.annuaire.artisans.device.entity.DeviceToken;
import bf.annuaire.artisans.device.mapper.DeviceTokenMapper;
import bf.annuaire.artisans.device.repository.DeviceTokenRepository;
import bf.annuaire.artisans.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestion des tokens FCM des appareils de l'utilisateur (enregistrement multi-appareil, purge des
 * tokens invalides). Le token doit être lié à l'utilisateur authentifié : c'est lui que cible l'envoi
 * de notifications push (feature {@code notification}).
 */
@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;
    private final DeviceTokenMapper deviceTokenMapper;

    /**
     * Enregistre ou rafraîchit un token. Upsert par {@code fcmToken} : si le token existe déjà, il est
     * réattribué à l'utilisateur courant (cas d'un appareil partagé après logout/login) et son
     * {@code lastSeenAt} est mis à jour ; sinon une nouvelle ligne est créée.
     */
    @Transactional
    public DeviceTokenDto register(AuthPrincipal principal, RegisterDeviceRequest request) {
        Instant now = Instant.now();
        DeviceToken token = deviceTokenRepository
                .findByFcmToken(request.fcmToken())
                .orElseGet(() -> {
                    DeviceToken fresh = new DeviceToken();
                    fresh.setCreatedAt(now);
                    return fresh;
                });
        token.setUser(userRepository.getReferenceById(principal.userId()));
        token.setFcmToken(request.fcmToken());
        token.setPlatform(request.platform());
        token.setLastSeenAt(now);
        return deviceTokenMapper.toDto(deviceTokenRepository.save(token));
    }

    @Transactional
    public void unregister(AuthPrincipal principal, String fcmToken) {
        deviceTokenRepository
                .findByFcmToken(fcmToken)
                .filter(token -> token.getUser().getId().equals(principal.userId()))
                .ifPresent(deviceTokenRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<DeviceTokenDto> listMine(AuthPrincipal principal) {
        return deviceTokenMapper.toDtoList(deviceTokenRepository.findByUserId(principal.userId()));
    }

    /** Tokens FCM (bruts) d'un utilisateur — utilisé par l'envoi de notifications. */
    @Transactional(readOnly = true)
    public List<String> tokensOf(Long userId) {
        return deviceTokenRepository.findByUserId(userId).stream()
                .map(DeviceToken::getFcmToken)
                .toList();
    }

    /** Purge un token rejeté par FCM (invalide / désinscrit). */
    @Transactional
    public void purgeInvalid(String fcmToken) {
        deviceTokenRepository.deleteByFcmToken(fcmToken);
    }
}
