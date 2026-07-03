package bf.annuaire.artisans.user.service;

import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.user.dto.UpdateProfileRequest;
import bf.annuaire.artisans.user.dto.UserDto;
import bf.annuaire.artisans.user.mapper.UserMapper;
import bf.annuaire.artisans.user.entity.Person;
import bf.annuaire.artisans.user.entity.Role;
import bf.annuaire.artisans.user.entity.RoleName;
import bf.annuaire.artisans.user.entity.User;
import bf.annuaire.artisans.user.entity.UserCredential;
import bf.annuaire.artisans.user.repository.PersonRepository;
import bf.annuaire.artisans.user.repository.RoleRepository;
import bf.annuaire.artisans.user.repository.UserRepository;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Gestion de l'agrégat utilisateur : création de l'ensemble {@code Person + User + UserCredential}
 * avec son rôle, et accès en lecture par téléphone.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private volatile Map<RoleName, Role> roleCache;

    /**
     * Crée un utilisateur complet (état civil, secrets, rôle) dans une transaction. Vérifie l'unicité
     * du téléphone et de l'email avant insertion.
     *
     * @throws BadRequestException si le téléphone ou l'email est déjà utilisé.
     */
    @Transactional
    public User createUser(
            String firstname,
            String lastname,
            String phone,
            String email,
            String rawPassword,
            RoleName roleName) {
        if (userRepository.existsByPhone(phone)) {
            throw new BadRequestException("Ce numéro de téléphone est déjà utilisé.");
        }
        if (StringUtils.hasText(email) && personRepository.existsByEmail(email)) {
            throw new BadRequestException("Cette adresse email est déjà utilisée.");
        }

        Role role = resolveRole(roleName);

        Person person = new Person(lastname, firstname, StringUtils.hasText(email) ? email : null);

        User user = new User();
        user.setPerson(person);
        user.setPhone(phone);
        user.setActive(true);
        user.addRole(role);
        user.setCredential(new UserCredential(passwordEncoder.encode(rawPassword)));

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getByPhone(String phone) {
        return userRepository
                .findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    /** Ajoute un rôle à un utilisateur existant (idempotent si déjà présent). */
    @Transactional
    public User grantRole(Long userId, RoleName roleName) {
        User user = userRepository
                .findWithDetailsById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        boolean alreadyHas = user.getRoles().stream().anyMatch(r -> r.getName() == roleName);
        if (alreadyHas) {
            return user;
        }
        Role role = resolveRole(roleName);
        user.addRole(role);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserDto getProfile(Long userId) {
        User user = userRepository
                .findWithDetailsById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository
                .findWithDetailsById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        Person person = user.getPerson();
        String email = StringUtils.hasText(request.email()) ? request.email().trim() : null;
        if (email != null
                && personRepository.existsByEmailAndIdNot(email, person.getId())) {
            throw new BadRequestException("Cette adresse email est déjà utilisée.");
        }
        person.setFirstname(request.firstname().trim());
        person.setLastname(request.lastname().trim());
        person.setEmail(email);
        person.setCity(StringUtils.hasText(request.city()) ? request.city().trim() : null);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    private Role resolveRole(RoleName roleName) {
        Map<RoleName, Role> cache = roleCache;
        if (cache == null) {
            synchronized (this) {
                cache = roleCache;
                if (cache == null) {
                    EnumMap<RoleName, Role> loaded = new EnumMap<>(RoleName.class);
                    for (Role role : roleRepository.findAll()) {
                        loaded.put(role.getName(), role);
                    }
                    roleCache = loaded;
                    cache = loaded;
                }
            }
        }
        Role role = cache.get(roleName);
        if (role == null) {
            throw new ResourceNotFoundException("Rôle introuvable : " + roleName);
        }
        return role;
    }
}
