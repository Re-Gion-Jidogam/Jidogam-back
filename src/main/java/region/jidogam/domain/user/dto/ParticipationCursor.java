package region.jidogam.domain.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import region.jidogam.common.dto.Cursor;

public record ParticipationCursor(
    LocalDateTime date, //participantAt or lastStampedAt
    UUID lastId
) {

  public static ParticipationCursor from(Cursor cursor) {
    if (cursor == null) {
      return null;
    }
    return new ParticipationCursor(
        LocalDateTime.parse(cursor.lastValue()),
        UUID.fromString(cursor.lastId())
    );
  }
}
