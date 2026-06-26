package bf.annuaire.artisans.ai.client;

/**
 * Évaluation brute d'un avis renvoyée par le modèle (signaux non pondérés). La pondération, la
 * détection de décalage note/texte et la décision de modération finale sont calculées en aval, de
 * façon déterministe et testable, par {@code ReviewAnalysisService}.
 *
 * @param textRating note 1-5 que le sentiment du texte justifierait (indépendamment de l'étoile saisie)
 * @param sentimentLabel {@code POSITIVE} / {@code NEUTRAL} / {@code NEGATIVE}
 * @param sentimentScore polarité -1.0 (très négatif) .. 1.0 (très positif)
 * @param fake avis vraisemblablement faux / abusif / spam
 * @param inappropriate contenu grossier / injurieux / inapproprié
 * @param reason courte justification (modération), ou {@code null}
 */
public record ReviewAssessment(
        int textRating,
        String sentimentLabel,
        double sentimentScore,
        boolean fake,
        boolean inappropriate,
        String reason) {}
