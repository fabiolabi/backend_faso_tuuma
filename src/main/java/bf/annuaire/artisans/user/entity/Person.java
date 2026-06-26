package bf.annuaire.artisans.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * État civil rattaché 1–1 à un {@link User} (table {@code person}).
 *
 * <p>L'{@code email} est optionnel et unique ; il sert de canal pour la réinitialisation du mot de
 * passe. {@code photoFileId} référence un fichier média (non géré dans la feature auth).
 */
@Entity
@Table(name = "person")
@Getter
@Setter
@NoArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "photo_file_id")
    private Long photoFileId;

    public Person(String lastname, String firstname, String email) {
        this.lastname = lastname;
        this.firstname = firstname;
        this.email = email;
    }
}
