package bf.annuaire.artisans.client.order.repository;

import bf.annuaire.artisans.client.order.entity.ServiceOrder;
import bf.annuaire.artisans.client.order.entity.ServiceOrderStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux demandes de prestation ({@code service_order}). */
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {

    Page<ServiceOrder> findByClientId(Long clientId, Pageable pageable);

    Page<ServiceOrder> findByClientIdAndStatus(Long clientId, ServiceOrderStatus status, Pageable pageable);

    /** Lecture scopée au client propriétaire (404 propre si la demande n'est pas la sienne). */
    Optional<ServiceOrder> findByIdAndClientId(Long id, Long clientId);

    /** Inbox de l'artisan : toutes les demandes adressées aux enseignes qu'il possède. */
    Page<ServiceOrder> findByMetier_Owner_Id(Long ownerId, Pageable pageable);

    Page<ServiceOrder> findByMetier_Owner_IdAndStatus(Long ownerId, ServiceOrderStatus status, Pageable pageable);

    long countByClientIdAndStatus(Long clientId, ServiceOrderStatus status);
}
