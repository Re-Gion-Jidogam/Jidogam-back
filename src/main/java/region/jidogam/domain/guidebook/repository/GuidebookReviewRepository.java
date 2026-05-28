package region.jidogam.domain.guidebook.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.guidebook.entity.GuidebookReview;

public interface GuidebookReviewRepository extends JpaRepository<GuidebookReview, UUID> {

  Optional<GuidebookReview> findByGuidebook_IdAndAuthor_Id(UUID guidebookId, UUID authorId);
}