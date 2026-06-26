package bf.annuaire.artisans.metier.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.metier.dto.AddGalleryItemRequest;
import bf.annuaire.artisans.metier.dto.CreateMetierRequest;
import bf.annuaire.artisans.metier.dto.GalleryItemDto;
import bf.annuaire.artisans.metier.dto.HourlyDto;
import bf.annuaire.artisans.metier.dto.MetierDetailDto;
import bf.annuaire.artisans.metier.dto.MetierSearchCriteria;
import bf.annuaire.artisans.metier.dto.MetierSummaryDto;
import bf.annuaire.artisans.metier.dto.ServiceDto;
import bf.annuaire.artisans.metier.dto.ServiceRequest;
import bf.annuaire.artisans.metier.dto.SocialMediaDto;
import bf.annuaire.artisans.metier.dto.UpdateMetierRequest;
import bf.annuaire.artisans.metier.service.MetierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Enseignes / commerces d'artisans ({@code /api/metiers}) — recherche de proximité publique,
 * publication et gestion réservées au propriétaire (rôle {@code ARTISAN}) ou à un {@code ADMIN}.
 * Les sous-ressources (services, horaires, réseaux sociaux, galerie) sont accrochées à l'enseigne.
 */
@RestController
@RequestMapping("/api/metiers")
@RequiredArgsConstructor
@Tag(name = "Métiers", description = "Annuaire géolocalisé des enseignes d'artisans")
public class MetierController {

    private final MetierService metierService;

    // ------------------------------------------------------------------- Recherche & lecture

    @Operation(summary = "Recherche de proximité paginée (enseignes publiées)")
    @GetMapping
    public Page<MetierSummaryDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        MetierSearchCriteria criteria = new MetierSearchCriteria(q, categorySlug, minRating, lat, lng, radiusKm);
        return metierService.search(criteria, PageRequest.of(page, size));
    }

    @Operation(summary = "Mes enseignes (propriétaire courant, publiées ou non)")
    @GetMapping("/mine")
    public Page<MetierSummaryDto> mine(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return metierService.listMine(principal, PageRequest.of(page, size));
    }

    @Operation(summary = "Détail d'une enseigne")
    @GetMapping("/{id}")
    public MetierDetailDto get(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.getDetail(id, principal);
    }

    // ------------------------------------------------------------------- Cycle de vie

    @Operation(summary = "Création d'une enseigne (ARTISAN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetierDetailDto create(
            @Valid @RequestBody CreateMetierRequest request, @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.create(principal, request);
    }

    @Operation(summary = "Mise à jour d'une enseigne (propriétaire ou ADMIN)")
    @PutMapping("/{id}")
    public MetierDetailDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMetierRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.update(principal, id, request);
    }

    @Operation(summary = "Publication d'une enseigne (propriétaire ou ADMIN)")
    @PostMapping("/{id}/publish")
    public MetierDetailDto publish(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.setPublished(principal, id, true);
    }

    @Operation(summary = "Dépublication d'une enseigne (propriétaire ou ADMIN)")
    @PostMapping("/{id}/unpublish")
    public MetierDetailDto unpublish(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.setPublished(principal, id, false);
    }

    @Operation(summary = "Suppression logique d'une enseigne (propriétaire ou ADMIN)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        metierService.delete(principal, id);
    }

    // ------------------------------------------------------------------- Services (prestations)

    @Operation(summary = "Prestations d'une enseigne")
    @GetMapping("/{id}/services")
    public List<ServiceDto> services(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.listServices(id, principal);
    }

    @Operation(summary = "Ajout d'une prestation (propriétaire ou ADMIN)")
    @PostMapping("/{id}/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceDto addService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.addService(principal, id, request);
    }

    @Operation(summary = "Mise à jour d'une prestation (propriétaire ou ADMIN)")
    @PutMapping("/{id}/services/{serviceId}")
    public ServiceDto updateService(
            @PathVariable Long id,
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.updateService(principal, id, serviceId, request);
    }

    @Operation(summary = "Suppression d'une prestation (propriétaire ou ADMIN)")
    @DeleteMapping("/{id}/services/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(
            @PathVariable Long id, @PathVariable Long serviceId, @AuthenticationPrincipal AuthPrincipal principal) {
        metierService.deleteService(principal, id, serviceId);
    }

    // ------------------------------------------------------------------- Horaires

    @Operation(summary = "Horaires d'ouverture d'une enseigne")
    @GetMapping("/{id}/hours")
    public List<HourlyDto> hours(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.listHours(id, principal);
    }

    @Operation(summary = "Remplacement des horaires (propriétaire ou ADMIN)")
    @PutMapping("/{id}/hours")
    public List<HourlyDto> replaceHours(
            @PathVariable Long id,
            @Valid @RequestBody List<HourlyDto> hours,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.replaceHours(principal, id, hours);
    }

    // ------------------------------------------------------------------- Réseaux sociaux

    @Operation(summary = "Liens réseaux sociaux d'une enseigne")
    @GetMapping("/{id}/socials")
    public List<SocialMediaDto> socials(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.listSocials(id, principal);
    }

    @Operation(summary = "Remplacement des liens réseaux sociaux (propriétaire ou ADMIN)")
    @PutMapping("/{id}/socials")
    public List<SocialMediaDto> replaceSocials(
            @PathVariable Long id,
            @Valid @RequestBody List<SocialMediaDto> socials,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.replaceSocials(principal, id, socials);
    }

    // ------------------------------------------------------------------- Galerie

    @Operation(summary = "Galerie d'images d'une enseigne")
    @GetMapping("/{id}/gallery")
    public List<GalleryItemDto> gallery(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.listGallery(id, principal);
    }

    @Operation(summary = "Ajout d'une image à la galerie (propriétaire ou ADMIN)")
    @PostMapping("/{id}/gallery")
    @ResponseStatus(HttpStatus.CREATED)
    public GalleryItemDto addGalleryItem(
            @PathVariable Long id,
            @Valid @RequestBody AddGalleryItemRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return metierService.addGalleryItem(principal, id, request);
    }

    @Operation(summary = "Retrait d'une image de la galerie (propriétaire ou ADMIN)")
    @DeleteMapping("/{id}/gallery/{galleryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGalleryItem(
            @PathVariable Long id, @PathVariable Long galleryId, @AuthenticationPrincipal AuthPrincipal principal) {
        metierService.deleteGalleryItem(principal, id, galleryId);
    }
}
