package region.jidogam.domain.stamp.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import region.jidogam.common.dto.Cursor;

public record StampCursor(
    LocalDateTime createdAt,
    UUID placeId
) {

  public static StampCursor from(Cursor cursor) {
    if (cursor == null) {
      return null;
    }
    return new StampCursor(
        LocalDateTime.parse(cursor.lastValue()),
        UUID.fromString(cursor.lastId())
    );
  }
}
