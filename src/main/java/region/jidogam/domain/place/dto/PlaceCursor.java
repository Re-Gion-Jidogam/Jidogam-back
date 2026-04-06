package region.jidogam.domain.place.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import region.jidogam.common.dto.Cursor;
import region.jidogam.common.exception.InvalidCursorException;

@Slf4j
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

    try {
      UUID lastId = UUID.fromString(cursor.lastId());

      return switch (sortBy) {
        case DISTANCE -> {
          String[] parts = cursor.lastValue().split(",");
          if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid distance cursor format");
          }
          yield PlaceCursor.builder()
              .distance(Double.parseDouble(parts[0]))
              .userLat(Double.parseDouble(parts[1]))
              .userLon(Double.parseDouble(parts[2]))
              .lastId(lastId)
              .build();
        }
        case STAMP_COUNT -> PlaceCursor.builder()
            .stampCount(Integer.parseInt(cursor.lastValue()))
            .lastId(lastId)
            .build();
      };

    } catch (Exception e) {
      log.warn("Invalid cursor format: {}", e.getMessage());
      throw new InvalidCursorException("Invalid cursor format");
    }
  }
}
