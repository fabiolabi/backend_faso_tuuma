package bf.annuaire.artisans.media.repository;

import bf.annuaire.artisans.media.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {}
