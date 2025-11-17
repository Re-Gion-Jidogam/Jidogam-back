package region.jidogam.domain.guidebook.event;

import java.util.UUID;

public record ImageDeleteEvent(
    String imageKey,
    String entityType,
    UUID entityId
) {

}
