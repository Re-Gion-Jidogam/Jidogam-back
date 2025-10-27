package region.jidogam.domain.guidebook.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import region.jidogam.common.dto.Cursor;

public record GuidebookCursor(
  Integer participantCount,
  LocalDateTime createdAt,
  UUID lastId
) {

  public static GuidebookCursor from(Cursor cursor, GuidebookSortBy sortBy) {
    if (cursor == null) {
      return null;
    }

    UUID lastId = UUID.fromString(cursor.lastId());

    return switch (sortBy) {
      case CREATED_AT -> new GuidebookCursor(
          null,
          LocalDateTime.parse(cursor.lastValue()),
          lastId
      );
      case PARTICIPANT_COUNT -> new GuidebookCursor(
          Integer.parseInt(cursor.lastValue()),
          null,
          lastId
      );
    };
  }

}
