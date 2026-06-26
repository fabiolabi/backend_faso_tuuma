package bf.annuaire.artisans.metier.service;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.metier.dto.CategoryDto;
import bf.annuaire.artisans.metier.dto.CreateCategoryRequest;
import bf.annuaire.artisans.metier.dto.UpdateCategoryRequest;
import bf.annuaire.artisans.metier.entity.Category;
import bf.annuaire.artisans.metier.mapper.CategoryMapper;
import bf.annuaire.artisans.metier.repository.CategoryRepository;
import bf.annuaire.artisans.user.entity.RoleName;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Gestion des catégories de métiers. Lecture publique (liste plate avec {@code parentId}, le mobile
 * reconstruit l'arbre) ; création / mise à jour / suppression réservées au rôle {@code ADMIN}.
 * Les catégories sont limitées à deux niveaux (une sous-catégorie ne peut pas avoir d'enfant).
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryDto> list() {
        return categoryMapper.toDtoList(categoryRepository.findAllByOrderByNameAsc());
    }

    @Transactional(readOnly = true)
    public CategoryDto get(Long id) {
        return categoryMapper.toDto(load(id));
    }

    @Transactional
    public CategoryDto create(AuthPrincipal principal, CreateCategoryRequest request) {
        assertAdmin(principal);
        Category category = new Category();
        category.setName(request.name());
        category.setSlug(normalizeSlug(request.slug()));
        category.setParent(resolveParent(request.parentId()));
        ensureSlugAvailable(category.getSlug(), null);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto update(AuthPrincipal principal, Long id, UpdateCategoryRequest request) {
        assertAdmin(principal);
        Category category = load(id);
        String slug = normalizeSlug(request.slug());
        ensureSlugAvailable(slug, id);
        category.setName(request.name());
        category.setSlug(slug);
        category.setParent(resolveParent(request.parentId(), id));
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(AuthPrincipal principal, Long id) {
        assertAdmin(principal);
        categoryRepository.delete(load(id));
    }

    private Category load(Long id) {
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
    }

    private Category resolveParent(Long parentId) {
        return resolveParent(parentId, null);
    }

    /** Résout le parent en garantissant : pas d'auto-référence, et profondeur ≤ 2 niveaux. */
    private Category resolveParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return null;
        }
        if (parentId.equals(selfId)) {
            throw new BadRequestException("Une catégorie ne peut pas être son propre parent.");
        }
        Category parent = load(parentId);
        if (parent.getParent() != null) {
            throw new BadRequestException("Les catégories sont limitées à deux niveaux.");
        }
        return parent;
    }

    private void ensureSlugAvailable(String slug, Long currentId) {
        if (slug == null) {
            return;
        }
        boolean taken = categoryRepository.existsBySlug(slug);
        if (taken && (currentId == null || !slug.equals(currentSlug(currentId)))) {
            throw new BadRequestException("Slug de catégorie déjà utilisé : " + slug);
        }
    }

    private String currentSlug(Long id) {
        return categoryRepository.findById(id).map(Category::getSlug).orElse(null);
    }

    private String normalizeSlug(String slug) {
        return StringUtils.hasText(slug) ? slug.trim().toLowerCase() : null;
    }

    private void assertAdmin(AuthPrincipal principal) {
        if (principal == null || !principal.roles().contains(RoleName.ADMIN.name())) {
            throw new AccessDeniedException("Action réservée à l'administrateur.");
        }
    }
}
