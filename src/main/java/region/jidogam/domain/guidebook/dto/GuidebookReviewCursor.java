package region.jidogam.domain.guidebook.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import region.jidogam.common.dto.Cursor;

public record GuidebookReviewCursor(
    LocalDateTime createdAt,
    UUID lastId
) {

  public static GuidebookReviewCursor from(Cursor cursor) {
    if (cursor == null) {
      return null;
    }
    return new GuidebookReviewCursor(
        LocalDateTime.parse(cursor.lastValue()),
        UUID.fromString(cursor.lastId())
    );
  }
}