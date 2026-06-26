package bf.annuaire.artisans.user.entity;

import bf.annuaire.artisans.common.AbstractAuditingEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Compte utilisateur (table {@code app_user}). Identifié au login par son {@code phone} (unique).
 *
 * <p>Porte l'état civil ({@link Person}, 1–1, cascade), les secrets ({@link UserCredential}, 1–1,
 * cascade) et ses {@link Role rôles} (N–N via {@code user_role}). Hérite de {@code createdAt} /
 * {@code updatedAt} (audit) via {@link AbstractAuditingEntity}.
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class User extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "phone", nullable = false, unique = true, length = 30)
    private String phone;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private UserCredential credential;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /** Associe un rôle à cet utilisateur. */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /** Lie réciproquement les secrets à cet utilisateur (côté propriétaire de la FK). */
    public void setCredential(UserCredential credential) {
        this.credential = credential;
        if (credential != null) {
            credential.setUser(this);
        }
    }
}
