package region.jidogam.domain.guidebook.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.guidebook.entity.GuidebookAreaRatio;

public interface GuidebookAreaRatioRepository extends JpaRepository<GuidebookAreaRatio, UUID> {

  void deleteByGuidebook_Id(UUID guidebookId);
}
