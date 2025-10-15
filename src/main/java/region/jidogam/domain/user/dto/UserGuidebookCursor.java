package region.jidogam.domain.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserGuidebookCursor(
    LocalDateTime date, //createdAt or updatedAt
    UUID lastId
) {

}
