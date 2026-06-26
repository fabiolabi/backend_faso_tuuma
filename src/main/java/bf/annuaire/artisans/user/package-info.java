/**
 * Feature <strong>user</strong> : identité de la plateforme — {@code Person}, {@code User}
 * ({@code app_user}), {@code UserCredential} (secrets isolés) et {@code Role} / {@code UserRole}.
 *
 * <p>Le login s'effectue par numéro de téléphone ({@code phone}, unique). Ce package porte les
 * entités, repositories et le service de création de l'agrégat utilisateur ; les flux
 * d'authentification (login, refresh, reset) vivent dans le package {@code auth}.
 */
package bf.annuaire.artisans.user;
