package region.jidogam.domain.stamp.repository.querydsl;

import java.util.List;
import java.util.UUID;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.stamp.dto.StampCursor;
import region.jidogam.domain.stamp.dto.StampSortBy;
import region.jidogam.domain.stamp.entity.Stamp;

public interface StampRepositoryCustom {

  List<Stamp> searchStampsByUserId(
      UUID userId,
      StampCursor cursor,
      String keyword,
      StampSortBy sortBy,
      SortDirection direction,
      int limit
  );

  long countStampsByUserId(UUID userId, String keyword);
}
