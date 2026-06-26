package bf.annuaire.artisans.device.repository;

import bf.annuaire.artisans.device.entity.DeviceToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    List<DeviceToken> findByUserId(Long userId);

    @Modifying
    void deleteByFcmToken(String fcmToken);
}
