package bf.annuaire.artisans.client.profile.service;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.messaging.repository.ConversationRepository;
import bf.annuaire.artisans.client.order.entity.ServiceOrderStatus;
import bf.annuaire.artisans.client.order.repository.ServiceOrderRepository;
import bf.annuaire.artisans.client.profile.dto.ClientSummaryDto;
import bf.annuaire.artisans.client.rating.repository.ServiceRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Espace client : agrégat en lecture seule de l'activité du client courant (demandes, notes, fils). */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ClientProfileService {

    private final ServiceOrderRepository orderRepository;
    private final ServiceRatingRepository ratingRepository;
    private final ConversationRepository conversationRepository;

    @Transactional(readOnly = true)
    public ClientSummaryDto summary(AuthPrincipal principal) {
        Long clientId = principal.userId();
        return new ClientSummaryDto(
                orderRepository.countByClientIdAndStatus(clientId, ServiceOrderStatus.PENDING),
                orderRepository.countByClientIdAndStatus(clientId, ServiceOrderStatus.ACCEPTED),
                orderRepository.countByClientIdAndStatus(clientId, ServiceOrderStatus.COMPLETED),
                orderRepository.countByClientIdAndStatus(clientId, ServiceOrderStatus.REJECTED),
                orderRepository.countByClientIdAndStatus(clientId, ServiceOrderStatus.CANCELLED),
                ratingRepository.countByClientId(clientId),
                conversationRepository.countByClientId(clientId));
    }
}
