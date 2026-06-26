package bf.annuaire.artisans.client;

import bf.annuaire.artisans.user.entity.Person;
import bf.annuaire.artisans.user.entity.User;

/**
 * Helper transverse à la feature {@code client} : compose le nom complet affichable d'un
 * utilisateur ({@code prénom nom}) de façon null-safe. Utilisé par les mappers pour exposer le nom
 * du client / de l'expéditeur sans fuiter l'entité {@link Person}.
 */
public final class PersonNames {

    private PersonNames() {}

    /** {@code "prénom nom"} épuré, ou {@code null} si l'utilisateur ou son état civil est absent/vide. */
    public static String fullName(User user) {
        if (user == null) {
            return null;
        }
        Person person = user.getPerson();
        if (person == null) {
            return null;
        }
        String first = person.getFirstname() != null ? person.getFirstname().trim() : "";
        String last = person.getLastname() != null ? person.getLastname().trim() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? null : full;
    }
}
