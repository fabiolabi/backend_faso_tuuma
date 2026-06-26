package bf.annuaire.artisans.metier.repository;

import bf.annuaire.artisans.metier.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsBySlug(String slug);

    /** Toutes les catégories triées par nom (le service reconstruit l'arbre racines → enfants). */
    List<Category> findAllByOrderByNameAsc();
}
