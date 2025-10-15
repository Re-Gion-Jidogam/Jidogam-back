package region.jidogam.domain.guidebook.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GuidebookCursor(
  Integer participantCount,
  LocalDateTime createdAt,
  UUID lastId
) {

}
