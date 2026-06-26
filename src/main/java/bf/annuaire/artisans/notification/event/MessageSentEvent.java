package bf.annuaire.artisans.notification.event;

/**
 * Émis après l'envoi d'un message dans une conversation. Déclenche, en asynchrone et après commit,
 * une notification à l'autre participant ({@code recipientUserId}). Porte le nom de l'expéditeur et un
 * aperçu du message pour éviter toute navigation lazy hors transaction côté écouteur.
 */
public record MessageSentEvent(Long conversationId, Long recipientUserId, String senderName, String preview) {}
