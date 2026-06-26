package bf.annuaire.artisans.metier.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.metier.dto.CategoryDto;
import bf.annuaire.artisans.metier.dto.CreateCategoryRequest;
import bf.annuaire.artisans.metier.dto.UpdateCategoryRequest;
import bf.annuaire.artisans.metier.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catégories de métiers ({@code /api/categories}). Consultation publique ; création / mise à jour /
 * suppression réservées au rôle {@code ADMIN} (contrôle dans {@link CategoryService}).
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Catégories", description = "Catégorisation des enseignes (lecture publique, gestion ADMIN)")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Liste des catégories (plate, avec parentId)")
    @GetMapping
    public List<CategoryDto> list() {
        return categoryService.list();
    }

    @Operation(summary = "Détail d'une catégorie")
    @GetMapping("/{id}")
    public CategoryDto get(@PathVariable Long id) {
        return categoryService.get(id);
    }

    @Operation(summary = "Création d'une catégorie (ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(
            @Valid @RequestBody CreateCategoryRequest request, @AuthenticationPrincipal AuthPrincipal principal) {
        return categoryService.create(principal, request);
    }

    @Operation(summary = "Mise à jour d'une catégorie (ADMIN)")
    @PutMapping("/{id}")
    public CategoryDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return categoryService.update(principal, id, request);
    }

    @Operation(summary = "Suppression d'une catégorie (ADMIN)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        categoryService.delete(principal, id);
    }
}
