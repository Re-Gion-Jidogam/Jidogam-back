package region.jidogam.common.dto;

import lombok.Builder;

@Builder
public record CoordinateRange(
    double latMin,
    double latMax,
    double lonMin,
    double lonMax
) {

}
