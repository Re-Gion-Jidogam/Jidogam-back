package region.jidogam.domain.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import region.jidogam.common.dto.Cursor;

public record UserGuidebookCursor(
    LocalDateTime date, //createdAt or updatedAt
    UUID lastId
) {

  public static UserGuidebookCursor from(Cursor cursor) {
    if (cursor == null) {
      return null;
    }
    return new UserGuidebookCursor(
        LocalDateTime.parse(cursor.lastValue()),
        UUID.fromString(cursor.lastId())
    );
  }
}
