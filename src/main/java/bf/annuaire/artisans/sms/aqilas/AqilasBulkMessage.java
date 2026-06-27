package bf.annuaire.artisans.sms.aqilas;

/** Entrée {@code messages[]} pour {@code POST /bulksms}. */
public record AqilasBulkMessage(String to, String text) {}
