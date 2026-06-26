package bf.annuaire.artisans.client.messaging.repository;

import bf.annuaire.artisans.client.messaging.entity.Conversation;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux fils de discussion ({@code conversation}). */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByClientIdAndMetierId(Long clientId, Long metierId);

    Page<Conversation> findByClientIdOrderByLastMessageAtDesc(Long clientId, Pageable pageable);

    Page<Conversation> findByMetier_Owner_IdOrderByLastMessageAtDesc(Long ownerId, Pageable pageable);

    long countByClientId(Long clientId);
}
