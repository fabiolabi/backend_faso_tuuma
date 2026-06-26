package bf.annuaire.artisans.auth.security;

import bf.annuaire.artisans.user.entity.User;
import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adaptateur {@link UserDetails} sur un {@link User} : exposé à Spring Security pour
 * l'authentification par mot de passe (login). Le username est le téléphone ; les autorités sont les
 * rôles préfixés {@code ROLE_}.
 */
public class AppUserDetails implements UserDetails {

    private final Long userId;
    private final String phone;
    private final String passwordHash;
    private final boolean active;
    private final Instant lockedUntil;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserDetails(User user) {
        this.userId = user.getId();
        this.phone = user.getPhone();
        this.passwordHash = user.getCredential() != null ? user.getCredential().getPasswordHash() : null;
        this.active = user.isActive();
        this.lockedUntil = user.getCredential() != null ? user.getCredential().getLockedUntil() : null;
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList());
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return phone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || lockedUntil.isBefore(Instant.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
