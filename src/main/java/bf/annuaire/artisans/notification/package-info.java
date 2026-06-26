/**
 * Feature <strong>notification</strong> : envoi des notifications push via Firebase Admin (FCM) et
 * historique persisté (centre de notifications mobile). Les évènements métier (nouvel avis,
 * changement de statut de demande, nouveau message) déclenchent, en asynchrone et après commit, la
 * création d'une notification + l'envoi push aux appareils du destinataire. Pilotée par
 * {@code app.notification.enabled} : à {@code false} (dev/test), repli {@code NoOpPushSender} sans
 * réseau.
 */
package bf.annuaire.artisans.notification;
