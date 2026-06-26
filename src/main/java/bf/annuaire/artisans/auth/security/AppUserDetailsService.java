package bf.annuaire.artisans.auth.security;

import bf.annuaire.artisans.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Charge un utilisateur par son téléphone (= username Spring Security) pour l'authentification par
 * mot de passe. Récupère en une requête l'état civil, les rôles et les secrets.
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        return userRepository
                .findByPhone(phone)
                .map(AppUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants invalides."));
    }
}
