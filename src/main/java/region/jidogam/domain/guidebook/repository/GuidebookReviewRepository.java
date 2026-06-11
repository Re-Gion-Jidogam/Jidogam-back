package region.jidogam.domain.guidebook.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.guidebook.entity.GuidebookReview;
import region.jidogam.domain.guidebook.repository.querydsl.GuidebookReviewRepositoryCustom;

public interface GuidebookReviewRepository extends JpaRepository<GuidebookReview, UUID>,
    GuidebookReviewRepositoryCustom {

  void deleteByGuidebook_Id(UUID guidebookId);

  Optional<GuidebookReview> findByGuidebook_IdAndAuthor_Id(UUID guidebookId, UUID authorId);
}
