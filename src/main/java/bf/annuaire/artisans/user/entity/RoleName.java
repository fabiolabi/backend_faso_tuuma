package bf.annuaire.artisans.user.entity;

/**
 * Rôles applicatifs (stockés en VARCHAR dans la table {@code role}, alimentée par la migration V2).
 *
 * <p>Un {@link User} peut être à la fois {@code CLIENT} et {@code ARTISAN}. {@code ADMIN} est réservé
 * à l'administration de la plateforme.
 */
public enum RoleName {
    CLIENT,
    ARTISAN,
    ADMIN
}
