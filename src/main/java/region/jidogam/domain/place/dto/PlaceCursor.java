package region.jidogam.domain.place.dto;

import java.util.UUID;
import lombok.Builder;
import region.jidogam.common.dto.Cursor;

@Builder
public record PlaceCursor(
    Integer stampCount,
    Double distance,
    UUID lastId
) {

  public static PlaceCursor from(Cursor cursor, PlaceSortBy sortBy) {
    if (cursor == null) {
      return null;
    }

    UUID lastId = UUID.fromString(cursor.lastId());

    return switch (sortBy) {
      case DISTANCE -> PlaceCursor.builder()
          .distance(Double.parseDouble(cursor.lastValue()))
          .lastId(lastId)
          .build();
      case STAMP_COUNT -> PlaceCursor.builder()
          .stampCount(Integer.parseInt(cursor.lastValue()))
          .lastId(lastId)
          .build();
    };
  }
}
