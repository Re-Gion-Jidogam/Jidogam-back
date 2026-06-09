package region.jidogam.domain.guidebook.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.guidebook.entity.GuidebookReview;

public interface GuidebookReviewRepository extends JpaRepository<GuidebookReview, UUID> {

  void deleteByGuidebook_Id(UUID guidebookId);
}
