package region.jidogam.domain.place.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PlaceResponse(

    UUID pid,
    String name,
    String address,
    double y,
    double x,
    LocalDateTime visitedDate,
    int guidebookCount,
    int stampCount,
    String category
) {

}
