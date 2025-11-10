package region.jidogam.domain.place.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlaceVisitInfo(
    UUID placeId,
    LocalDateTime visitedDate
) {

}
