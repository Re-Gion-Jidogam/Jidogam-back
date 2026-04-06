package region.jidogam.domain.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import region.jidogam.common.dto.Cursor;

public record GuidebookParticipationCursor(
    LocalDateTime lastActivityAt,
    UUID lastId
) {

  public static GuidebookParticipationCursor from(Cursor cursor) {
    if (cursor == null) {
      return null;
    }
    return new GuidebookParticipationCursor(
        LocalDateTime.parse(cursor.lastValue()),
        UUID.fromString(cursor.lastId())
    );
  }
}
