package bf.annuaire.artisans.search.dto;

/**
 * Suggestion d'autocomplétion pour la barre de recherche mobile.
 *
 * <p>{@code type} indique l'origine ({@code CATEGORY}, {@code METIER} ou {@code SERVICE}) afin que le
 * mobile sache comment réagir au tap : pour une catégorie, {@code value} porte le {@code slug} à
 * passer en filtre ({@code categorySlug}) ; pour une enseigne ou une prestation, {@code value} reprend
 * le libellé à relancer comme requête texte ({@code q}).
 */
public record SearchSuggestionDto(String type, String label, String value) {}
