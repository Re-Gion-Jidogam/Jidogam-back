package region.jidogam.domain.place.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlaceGuidebookCountRequest(
    @NotNull
    @Size(min = 1, max = 100)
    List<String> kakaoPids
) {

}
