package bf.annuaire.artisans.metier.service;

import bf.annuaire.artisans.ai.event.MetierContentChangedEvent;
import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.common.util.GeoUtils;
import bf.annuaire.artisans.media.entity.MediaFile;
import bf.annuaire.artisans.media.repository.MediaFileRepository;
import bf.annuaire.artisans.media.service.MediaUrlService;
import bf.annuaire.artisans.metier.dto.AddGalleryItemRequest;
import bf.annuaire.artisans.metier.dto.CreateMetierRequest;
import bf.annuaire.artisans.metier.dto.GalleryItemDto;
import bf.annuaire.artisans.metier.dto.HourlyDto;
import bf.annuaire.artisans.metier.dto.MetierDetailDto;
import bf.annuaire.artisans.metier.dto.MetierPhoneDto;
import bf.annuaire.artisans.metier.dto.MetierSearchCriteria;
import bf.annuaire.artisans.metier.dto.MetierSummaryDto;
import bf.annuaire.artisans.metier.dto.MetierWriteRequest;
import bf.annuaire.artisans.metier.dto.ServiceDto;
import bf.annuaire.artisans.metier.dto.ServiceRequest;
import bf.annuaire.artisans.metier.dto.SocialMediaDto;
import bf.annuaire.artisans.metier.dto.UpdateMetierRequest;
import bf.annuaire.artisans.metier.entity.Address;
import bf.annuaire.artisans.metier.entity.Category;
import bf.annuaire.artisans.metier.entity.Hourly;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.entity.MetierGallery;
import bf.annuaire.artisans.metier.entity.MetierPhone;
import bf.annuaire.artisans.metier.entity.MetierSocialMedia;
import bf.annuaire.artisans.metier.mapper.CategoryMapper;
import bf.annuaire.artisans.metier.mapper.MetierMapper;
import bf.annuaire.artisans.metier.mapper.ServiceMapper;
import bf.annuaire.artisans.metier.repository.AddressRepository;
import bf.annuaire.artisans.metier.repository.CategoryRepository;
import bf.annuaire.artisans.metier.repository.HourlyRepository;
import bf.annuaire.artisans.metier.repository.MetierGalleryRepository;
import bf.annuaire.artisans.metier.repository.MetierPhoneRepository;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import bf.annuaire.artisans.metier.repository.MetierSocialMediaRepository;
import bf.annuaire.artisans.metier.repository.ServiceRepository;
import bf.annuaire.artisans.user.entity.RoleName;
import bf.annuaire.artisans.user.repository.UserRepository;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cœur métier de l'annuaire : cycle de vie d'une enseigne ({@link Metier}) et de son agrégat
 * (adresse, catégories, services, horaires, réseaux sociaux, galerie), publication et recherche de
 * proximité. La notation est portée par les services et calculée par l'IA (features
 * {@code client.rating} + {@code ai}) : elle est ici en lecture seule.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class MetierService {

    private final MetierRepository metierRepository;
    private final AddressRepository addressRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;
    private final HourlyRepository hourlyRepository;
    private final MetierSocialMediaRepository socialMediaRepository;
    private final MetierPhoneRepository phoneRepository;
    private final MetierGalleryRepository galleryRepository;
    private final MediaFileRepository mediaFileRepository;
    private final UserRepository userRepository;
    private final MetierMapper metierMapper;
    private final MediaUrlService mediaUrlService;
    private final ServiceMapper serviceMapper;
    private final CategoryMapper categoryMapper;
    private final ApplicationEventPublisher events;

    // ----------------------------------------------------------------- Recherche & lecture

    /** Recherche de proximité paginée (enseignes publiées et actives uniquement). */
    @Transactional(readOnly = true)
    public Page<MetierSummaryDto> search(MetierSearchCriteria criteria, Pageable pageable) {
        return metierRepository
                .search(
                        criteria.q(),
                        criteria.categorySlug(),
                        criteria.lat(),
                        criteria.lng(),
                        criteria.radiusKm(),
                        pageable)
                .map(metier -> metierMapper.toSummary(metier, distanceFor(metier, criteria)));
    }

    /** Enseignes du propriétaire courant (publiées ou non). */
    @Transactional(readOnly = true)
    public Page<MetierSummaryDto> listMine(AuthPrincipal principal, Pageable pageable) {
        return metierRepository
                .findByOwnerId(principal.userId(), pageable)
                .map(metier -> metierMapper.toSummary(metier, null));
    }

    /** Détail d'une enseigne ; 404 si non publiée/inactive et que l'appelant n'est ni propriétaire ni admin. */
    @Transactional(readOnly = true)
    public MetierDetailDto getDetail(Long id, AuthPrincipal principal) {
        Metier metier = metierRepository
                .findWithDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enseigne introuvable : " + id));
        if (!isVisible(metier) && !isOwnerOrAdmin(metier, principal)) {
            throw new ResourceNotFoundException("Enseigne introuvable : " + id);
        }
        return toDetail(metier);
    }

    // ----------------------------------------------------------------- Cycle de vie

    @Transactional
    public MetierDetailDto create(AuthPrincipal principal, CreateMetierRequest request) {
        requireArtisan(principal);
        Metier metier = new Metier();
        metier.setOwner(userRepository.getReferenceById(principal.userId()));
        applyWritableFields(metier, request);
        Metier saved = metierRepository.save(metier);
        events.publishEvent(new MetierContentChangedEvent(saved.getId()));
        return toDetail(saved);
    }

    @Transactional
    public MetierDetailDto update(AuthPrincipal principal, Long id, UpdateMetierRequest request) {
        Metier metier = loadOwned(id, principal);
        applyWritableFields(metier, request);
        Metier saved = metierRepository.save(metier);
        events.publishEvent(new MetierContentChangedEvent(saved.getId()));
        return toDetail(saved);
    }

    /**
     * Applique les champs modifiables (communs création/mise à jour). {@code address},
     * {@code categoryIds} et {@code coverFileId} à {@code null} laissent la valeur existante
     * inchangée ; une liste de catégories vide ({@code []}) détache toutes les catégories.
     */
    private void applyWritableFields(Metier metier, MetierWriteRequest request) {
        metier.setName(request.name());
        metier.setDescription(request.description());
        metier.setAddressDescription(request.addressDescription());
        metier.setGpsLat(request.gpsLat());
        metier.setGpsLng(request.gpsLng());
        if (request.address() != null) {
            Address target = metier.getAddress() != null ? metier.getAddress() : new Address();
            metier.setAddress(saveAddress(target, request.address()));
        }
        if (request.categoryIds() != null) {
            metier.setCategories(resolveCategories(request.categoryIds()));
        }
        if (request.coverFileId() != null) {
            metier.setCover(resolveFile(request.coverFileId()));
        }
    }

    @Transactional
    public MetierDetailDto setPublished(AuthPrincipal principal, Long id, boolean published) {
        Metier metier = loadOwned(id, principal);
        metier.setPublished(published);
        Metier saved = metierRepository.save(metier);
        events.publishEvent(new MetierContentChangedEvent(saved.getId()));
        return toDetail(saved);
    }

    /** Suppression logique : l'enseigne disparaît de la recherche, les données sont conservées. */
    @Transactional
    public void delete(AuthPrincipal principal, Long id) {
        Metier metier = loadOwned(id, principal);
        metier.setActive(false);
        metierRepository.save(metier);
    }

    // ----------------------------------------------------------------- Services (prestations)

    @Transactional(readOnly = true)
    public List<ServiceDto> listServices(Long metierId, AuthPrincipal principal) {
        loadVisible(metierId, principal);
        return serviceMapper.toDtoList(serviceRepository.findByMetierIdOrderByIdAsc(metierId));
    }

    @Transactional
    public ServiceDto addService(AuthPrincipal principal, Long metierId, ServiceRequest request) {
        Metier metier = loadOwned(metierId, principal);
        bf.annuaire.artisans.metier.entity.Service service = new bf.annuaire.artisans.metier.entity.Service();
        service.setMetier(metier);
        applyService(service, request);
        ServiceDto dto = serviceMapper.toDto(serviceRepository.save(service));
        events.publishEvent(new MetierContentChangedEvent(metierId));
        return dto;
    }

    @Transactional
    public ServiceDto updateService(AuthPrincipal principal, Long metierId, Long serviceId, ServiceRequest request) {
        loadOwned(metierId, principal);
        bf.annuaire.artisans.metier.entity.Service service = serviceRepository
                .findById(serviceId)
                .filter(s -> s.getMetier().getId().equals(metierId))
                .orElseThrow(() -> new ResourceNotFoundException("Prestation introuvable : " + serviceId));
        applyService(service, request);
        ServiceDto dto = serviceMapper.toDto(serviceRepository.save(service));
        events.publishEvent(new MetierContentChangedEvent(metierId));
        return dto;
    }

    @Transactional
    public void deleteService(AuthPrincipal principal, Long metierId, Long serviceId) {
        loadOwned(metierId, principal);
        bf.annuaire.artisans.metier.entity.Service service = serviceRepository
                .findById(serviceId)
                .filter(s -> s.getMetier().getId().equals(metierId))
                .orElseThrow(() -> new ResourceNotFoundException("Prestation introuvable : " + serviceId));
        serviceRepository.delete(service);
        events.publishEvent(new MetierContentChangedEvent(metierId));
    }

    // ----------------------------------------------------------------- Horaires

    @Transactional(readOnly = true)
    public List<HourlyDto> listHours(Long metierId, AuthPrincipal principal) {
        loadVisible(metierId, principal);
        return metierMapper.toHourlyDtoList(hoursOf(metierId));
    }

    /** Remplace intégralement les horaires de l'enseigne (au plus une ligne par jour). */
    @Transactional
    public List<HourlyDto> replaceHours(AuthPrincipal principal, Long metierId, List<HourlyDto> items) {
        Metier metier = loadOwned(metierId, principal);
        EnumSet<java.time.DayOfWeek> seen = EnumSet.noneOf(java.time.DayOfWeek.class);
        for (HourlyDto item : items) {
            if (item.day() == null) {
                throw new BadRequestException("Jour manquant dans les horaires.");
            }
            if (!seen.add(item.day())) {
                throw new BadRequestException("Jour en double dans les horaires : " + item.day());
            }
        }
        hourlyRepository.deleteByMetierId(metierId);
        hourlyRepository.flush();
        for (HourlyDto item : items) {
            Hourly hourly = new Hourly();
            hourly.setMetier(metier);
            hourly.setDay(item.day());
            hourly.setOpenHour(item.openHour());
            hourly.setCloseHour(item.closeHour());
            hourly.setOpen(item.open());
            hourlyRepository.save(hourly);
        }
        return metierMapper.toHourlyDtoList(hoursOf(metierId));
    }

    // ----------------------------------------------------------------- Réseaux sociaux

    @Transactional(readOnly = true)
    public List<SocialMediaDto> listSocials(Long metierId, AuthPrincipal principal) {
        loadVisible(metierId, principal);
        return metierMapper.toSocialDtoList(socialMediaRepository.findByMetierIdOrderByIdAsc(metierId));
    }

    /** Remplace intégralement les liens réseaux sociaux de l'enseigne. */
    @Transactional
    public List<SocialMediaDto> replaceSocials(AuthPrincipal principal, Long metierId, List<SocialMediaDto> items) {
        Metier metier = loadOwned(metierId, principal);
        for (SocialMediaDto item : items) {
            if (item.platform() == null || item.url() == null || item.url().isBlank()) {
                throw new BadRequestException("Plateforme et URL obligatoires pour chaque lien.");
            }
        }
        socialMediaRepository.deleteByMetierId(metierId);
        socialMediaRepository.flush();
        for (SocialMediaDto item : items) {
            MetierSocialMedia social = new MetierSocialMedia();
            social.setMetier(metier);
            social.setPlatform(item.platform());
            social.setUrl(item.url());
            socialMediaRepository.save(social);
        }
        return metierMapper.toSocialDtoList(socialMediaRepository.findByMetierIdOrderByIdAsc(metierId));
    }

    // ----------------------------------------------------------------- Numéros de téléphone

    @Transactional(readOnly = true)
    public List<MetierPhoneDto> listPhones(Long metierId, AuthPrincipal principal) {
        loadVisible(metierId, principal);
        return metierMapper.toPhoneDtoList(phoneRepository.findByMetierIdOrderByIdAsc(metierId));
    }

    /** Remplace intégralement les numéros de contact de l'enseigne. */
    @Transactional
    public List<MetierPhoneDto> replacePhones(AuthPrincipal principal, Long metierId, List<MetierPhoneDto> items) {
        Metier metier = loadOwned(metierId, principal);
        for (MetierPhoneDto item : items) {
            if (item.number() == null || item.number().isBlank()) {
                throw new BadRequestException("Numéro obligatoire pour chaque téléphone.");
            }
        }
        phoneRepository.deleteByMetierId(metierId);
        phoneRepository.flush();
        for (MetierPhoneDto item : items) {
            MetierPhone phone = new MetierPhone();
            phone.setMetier(metier);
            phone.setNumber(item.number());
            phone.setWhatsapp(item.whatsapp());
            phone.setLabel(item.label());
            phoneRepository.save(phone);
        }
        events.publishEvent(new MetierContentChangedEvent(metierId));
        return metierMapper.toPhoneDtoList(phoneRepository.findByMetierIdOrderByIdAsc(metierId));
    }

    // ----------------------------------------------------------------- Galerie

    @Transactional(readOnly = true)
    public List<GalleryItemDto> listGallery(Long metierId, AuthPrincipal principal) {
        loadVisible(metierId, principal);
        return metierMapper.toGalleryDtoList(galleryOf(metierId));
    }

    @Transactional
    public GalleryItemDto addGalleryItem(AuthPrincipal principal, Long metierId, AddGalleryItemRequest request) {
        Metier metier = loadOwned(metierId, principal);
        MetierGallery item = new MetierGallery();
        item.setMetier(metier);
        item.setFile(resolveFile(request.fileId()));
        item.setPosition(request.position());
        return metierMapper.toGalleryDto(galleryRepository.save(item));
    }

    @Transactional
    public void deleteGalleryItem(AuthPrincipal principal, Long metierId, Long galleryId) {
        loadOwned(metierId, principal);
        MetierGallery item = galleryRepository
                .findByIdAndMetierId(galleryId, metierId)
                .orElseThrow(() -> new ResourceNotFoundException("Image de galerie introuvable : " + galleryId));
        galleryRepository.delete(item);
    }

    // ----------------------------------------------------------------- Helpers internes

    private Metier loadOwned(Long id, AuthPrincipal principal) {
        Metier metier = metierRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enseigne introuvable : " + id));
        if (!isOwnerOrAdmin(metier, principal)) {
            throw new AccessDeniedException("Action réservée au propriétaire de l'enseigne.");
        }
        return metier;
    }

    private Metier loadVisible(Long id, AuthPrincipal principal) {
        Metier metier = metierRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enseigne introuvable : " + id));
        if (!isVisible(metier) && !isOwnerOrAdmin(metier, principal)) {
            throw new ResourceNotFoundException("Enseigne introuvable : " + id);
        }
        return metier;
    }

    private Address saveAddress(Address target, bf.annuaire.artisans.metier.dto.AddressDto dto) {
        target.setCity(dto.city());
        target.setDistrict(dto.district());
        target.setSector(dto.sector());
        target.setStreet(dto.street());
        return addressRepository.save(target);
    }

    private Set<Category> resolveCategories(Set<Long> ids) {
        Set<Category> result = new LinkedHashSet<>();
        for (Long categoryId : ids) {
            result.add(categoryRepository
                    .findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + categoryId)));
        }
        return result;
    }

    private MediaFile resolveFile(Long fileId) {
        return mediaFileRepository
                .findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Fichier introuvable : " + fileId));
    }

    private void applyService(bf.annuaire.artisans.metier.entity.Service service, ServiceRequest request) {
        if (request.priceMin() != null && request.priceMax() != null && request.priceMin() > request.priceMax()) {
            throw new BadRequestException("Le prix minimum dépasse le prix maximum.");
        }
        service.setName(request.name());
        service.setDescription(request.description());
        service.setPriceMin(request.priceMin());
        service.setPriceMax(request.priceMax());
        service.setActive(request.active() == null || request.active());
    }

    private MetierDetailDto toDetail(Metier metier) {
        List<bf.annuaire.artisans.metier.entity.Service> services =
                serviceRepository.findByMetierIdOrderByIdAsc(metier.getId());
        List<Hourly> hours = hoursOf(metier.getId());
        List<MetierSocialMedia> socials = socialMediaRepository.findByMetierIdOrderByIdAsc(metier.getId());
        List<MetierPhone> phones = phoneRepository.findByMetierIdOrderByIdAsc(metier.getId());
        List<MetierGallery> gallery = galleryOf(metier.getId());
        return new MetierDetailDto(
                metier.getId(),
                metier.getOwner().getId(),
                metier.getName(),
                metierMapper.toPhoneDtoList(phones),
                metier.getDescription(),
                metier.getAddressDescription(),
                metier.getAddress() != null ? metierMapper.toAddressDto(metier.getAddress()) : null,
                mediaUrlService.mediaUrl(metier.getCover()),
                metier.getGpsLat(),
                metier.getGpsLng(),
                metier.isPublished(),
                metier.isActive(),
                metier.getRatingAvg(),
                metier.getRatingCount(),
                metier.getAiSummary(),
                categoryMapper.toDtoList(metier.getCategories()),
                serviceMapper.toDtoList(services),
                metierMapper.toHourlyDtoList(hours),
                metierMapper.toSocialDtoList(socials),
                metierMapper.toGalleryDtoList(gallery),
                metier.getCreatedAt(),
                metier.getUpdatedAt());
    }

    /** Horaires triés par jour (MONDAY→SUNDAY) ; le tri ne peut pas être fait en SQL ({@code day} réservé). */
    private List<Hourly> hoursOf(Long metierId) {
        return hourlyRepository.findByMetierId(metierId).stream()
                .sorted(Comparator.comparing(Hourly::getDay))
                .toList();
    }

    /** Galerie triée par {@code position} (nulls en dernier) puis id ; tri en Java ({@code position} réservé). */
    private List<MetierGallery> galleryOf(Long metierId) {
        return galleryRepository.findByMetierId(metierId).stream()
                .sorted(Comparator.comparing(
                                MetierGallery::getPosition, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MetierGallery::getId))
                .toList();
    }

    private Double distanceFor(Metier metier, MetierSearchCriteria criteria) {
        if (criteria.lat() == null
                || criteria.lng() == null
                || metier.getGpsLat() == null
                || metier.getGpsLng() == null) {
            return null;
        }
        return GeoUtils.haversineKm(
                criteria.lat(), criteria.lng(), metier.getGpsLat().doubleValue(), metier.getGpsLng().doubleValue());
    }

    private boolean isVisible(Metier metier) {
        return metier.isPublished() && metier.isActive();
    }

    private boolean isOwnerOrAdmin(Metier metier, AuthPrincipal principal) {
        if (principal == null) {
            return false;
        }
        if (principal.roles().contains(RoleName.ADMIN.name())) {
            return true;
        }
        return metier.getOwner() != null && metier.getOwner().getId().equals(principal.userId());
    }

    private void requireArtisan(AuthPrincipal principal) {
        if (principal == null
                || (!principal.roles().contains(RoleName.ARTISAN.name())
                        && !principal.roles().contains(RoleName.ADMIN.name()))) {
            throw new AccessDeniedException("Seul un artisan peut créer une enseigne.");
        }
    }
}
