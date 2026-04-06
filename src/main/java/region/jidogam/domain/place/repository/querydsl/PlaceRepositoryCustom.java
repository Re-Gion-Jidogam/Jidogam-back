package region.jidogam.domain.place.repository.querydsl;

import java.util.List;
import java.util.UUID;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.place.dto.PlaceCursor;
import region.jidogam.domain.place.dto.PlaceFilter;
import region.jidogam.domain.place.dto.PlaceSortBy;
import region.jidogam.domain.place.entity.Place;

public interface PlaceRepositoryCustom {

  List<Place> searchPlaceByGuidebook(
      UUID guidebookId,
      UUID userId,
      PlaceFilter filter,
      PlaceCursor cursor,
      PlaceSortBy sortBy,
      SortDirection direction,
      int limit
  );
}
