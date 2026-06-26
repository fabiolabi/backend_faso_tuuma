package bf.annuaire.artisans.client.order.entity;

/**
 * Cycle de vie d'une demande de prestation ({@code service_order.status}).
 *
 * <p>Transitions autorisées : {@code PENDING → ACCEPTED | REJECTED} (par l'artisan),
 * {@code ACCEPTED → COMPLETED} (par l'artisan), {@code PENDING | ACCEPTED → CANCELLED} (par le
 * client). {@code REJECTED}, {@code COMPLETED} et {@code CANCELLED} sont terminaux.
 */
public enum ServiceOrderStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    COMPLETED,
    CANCELLED
}
