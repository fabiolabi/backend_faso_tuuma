package bf.annuaire.artisans.client.favorite.service;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.favorite.entity.MetierFavorite;
import bf.annuaire.artisans.client.favorite.repository.MetierFavoriteRepository;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.metier.dto.MetierSummaryDto;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.mapper.MetierMapper;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import bf.annuaire.artisans.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Favoris d'un utilisateur : marquer / retirer une enseigne et lister ses favoris. Réservé à un
 * utilisateur authentifié (toute personne connectée, sans contrainte de rôle). La liste réutilise la
 * vue résumée d'enseigne ({@link MetierSummaryDto}) pour que le mobile affiche directement la carte.
 */
@Service
@RequiredArgsConstructor
public class MetierFavoriteService {

    private final MetierFavoriteRepository favoriteRepository;
    private final MetierRepository metierRepository;
    private final UserRepository userRepository;
    private final MetierMapper metierMapper;

    /** Ajoute l'enseigne aux favoris du client courant. Idempotent (no-op si déjà en favori). */
    @Transactional
    public void add(AuthPrincipal principal, Long metierId) {
        Metier metier = loadVisibleMetier(metierId);
        if (favoriteRepository.existsByClientIdAndMetierId(principal.userId(), metierId)) {
            return;
        }
        MetierFavorite favorite = new MetierFavorite();
        favorite.setClient(userRepository.getReferenceById(principal.userId()));
        favorite.setMetier(metier);
        favoriteRepository.save(favorite);
    }

    /** Retire l'enseigne des favoris du client courant. Idempotent. */
    @Transactional
    public void remove(AuthPrincipal principal, Long metierId) {
        favoriteRepository.deleteByClientIdAndMetierId(principal.userId(), metierId);
    }

    @Transactional(readOnly = true)
    public Page<MetierSummaryDto> listMine(AuthPrincipal principal, Pageable pageable) {
        return favoriteRepository
                .findByClientIdOrderByCreatedAtDesc(principal.userId(), pageable)
                .map(favorite -> metierMapper.toSummary(favorite.getMetier(), null));
    }

    private Metier loadVisibleMetier(Long metierId) {
        Metier metier = metierRepository
                .findById(metierId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseigne introuvable : " + metierId));
        if (!metier.isPublished() || !metier.isActive()) {
            throw new ResourceNotFoundException("Enseigne introuvable : " + metierId);
        }
        return metier;
    }
}
