package region.jidogam.domain.guidebook.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.guidebook.entity.GuidebookReview;

public interface GuidebookReviewRepository extends JpaRepository<GuidebookReview, UUID> {

  boolean existsByGuidebook_IdAndAuthor_Id(UUID guidebookId, UUID authorId);
}