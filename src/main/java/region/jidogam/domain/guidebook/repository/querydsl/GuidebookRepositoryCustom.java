package region.jidogam.domain.guidebook.repository.querydsl;

import java.util.List;
import java.util.UUID;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.GuidebookCursor;
import region.jidogam.domain.guidebook.dto.GuidebookSortBy;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.user.dto.UserGuideBookSortBy;
import region.jidogam.domain.user.dto.UserGuidebookCursor;

public interface GuidebookRepositoryCustom {

  List<Guidebook> searchGuidebook(
      GuidebookCursor cursor,
      String keyword,
      GuidebookSortBy sortBy,
      SortDirection direction,
      int limit
  );

  List<Guidebook> searchGuidebookByAuthorId(
      UUID authorId,
      UserGuidebookCursor cursor,
      String keyword,
      UserGuideBookSortBy sortBy,
      SortDirection direction,
      int limit);
}
