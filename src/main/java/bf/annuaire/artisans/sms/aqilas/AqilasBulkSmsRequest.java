package bf.annuaire.artisans.sms.aqilas;

import java.util.List;

/**
 * Corps JSON {@code POST /bulksms} — AQILAS API v1.
 *
 * <p>Permet d'envoyer plusieurs SMS avec un contenu différent par destinataire.
 */
public record AqilasBulkSmsRequest(String from, List<AqilasBulkMessage> messages) {}
