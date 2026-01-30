package region.jidogam.domain.place.dto;

import java.util.UUID;

public record PlaceGuidebookCountResponse(
    UUID pid,
    String kakaoPid,
    int guidebookCount
) {

}
