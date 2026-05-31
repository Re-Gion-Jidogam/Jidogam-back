package region.jidogam.domain.guidebook.repository.querydsl;

import java.util.List;
import java.util.UUID;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.GuidebookReviewCursor;
import region.jidogam.domain.guidebook.entity.GuidebookReview;

public interface GuidebookReviewRepositoryCustom {

  List<GuidebookReview> searchByGuidebookId(
      UUID guidebookId,
      GuidebookReviewCursor cursor,
      SortDirection direction,
      int limit
  );

  long countByGuidebookId(UUID guidebookId);
}