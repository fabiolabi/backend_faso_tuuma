/**
 * Feature <strong>client</strong> : tout ce que fait un citoyen (rôle {@code CLIENT}) au-delà de la
 * simple recherche d'enseignes. Regroupe quatre sous-domaines, chacun avec ses propres couches
 * (controller / service / repository / entity / dto / mapper) :
 *
 * <ul>
 *   <li>{@code order} — demandes de prestation ({@code service_order}) : un client sollicite une
 *       enseigne ; l'artisan propriétaire accepte / refuse / clôture, le client annule.
 *   <li>{@code rating} — notation d'une enseigne ({@code metier_rating}, note 1–5 + commentaire, une
 *       par client et par enseigne) ; recalcule {@code metier.rating_avg} / {@code rating_count}.
 *   <li>{@code messaging} — messagerie client↔artisan ({@code conversation} / {@code message})
 *       persistée et exposée en REST (le temps-réel WebSocket reste une feature future).
 *   <li>{@code profile} — espace client : agrégat de l'activité (résumé des demandes, notes,
 *       conversations).
 * </ul>
 *
 * <p>Note : la notation portée ici par {@code metier_rating} remplace l'ancien modèle
 * {@code comment} / Gemini (table {@code Comment} et colonne {@code ai_rating} absentes du schéma
 * réel). Les notifications push (FCM) déclenchées par ces actions relèvent de la feature
 * {@code notification}, non câblée à ce stade.
 */
package bf.annuaire.artisans.client;
