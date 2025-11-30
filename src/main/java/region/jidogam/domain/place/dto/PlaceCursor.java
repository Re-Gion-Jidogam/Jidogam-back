package region.jidogam.domain.place.dto;

import java.util.UUID;
import lombok.Builder;
import region.jidogam.common.dto.Cursor;

@Builder
public record PlaceCursor(
    Integer stampCount,
    Double distance,
    Double userLat,
    Double userLon,
    UUID lastId
) {

  public static PlaceCursor from(Cursor cursor, PlaceSortBy sortBy) {
    if (cursor == null) {
      return null;
    }

    UUID lastId = UUID.fromString(cursor.lastId());

    PlaceCursor placeCursor = null;
    switch (sortBy) {
      case DISTANCE -> {
        String[] parts = cursor.lastValue().split(",");
        placeCursor = PlaceCursor.builder()
            .distance(Double.parseDouble(parts[0]))
            .userLat(Double.parseDouble(parts[1]))
            .userLon(Double.parseDouble(parts[2]))
            .lastId(lastId)
            .build();
      }
      case STAMP_COUNT -> placeCursor = PlaceCursor.builder()
          .stampCount(Integer.parseInt(cursor.lastValue()))
          .lastId(lastId)
          .build();
    }
    return placeCursor;
  }
}
