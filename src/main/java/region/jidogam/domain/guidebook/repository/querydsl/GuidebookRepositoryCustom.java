package region.jidogam.domain.guidebook.repository.querydsl;

import java.util.List;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.GuidebookCursor;
import region.jidogam.domain.guidebook.dto.GuidebookSortBy;
import region.jidogam.domain.guidebook.entity.Guidebook;

public interface GuidebookRepositoryCustom {

  List<Guidebook> searchGuidebook(
      GuidebookCursor cursor,
      String keyword,
      GuidebookSortBy sortBy,
      SortDirection direction,
      Boolean isLocal,
      int limit
  );

  long countPublishedGuidebooksByKeyword(String keyword, Boolean isLocal);
}
