package bf.annuaire.artisans.sms.model;

/** SMS personnalisé : un destinataire et son texte (E.164). */
public record SmsPersonalizedMessage(String phoneE164, String text) {}
