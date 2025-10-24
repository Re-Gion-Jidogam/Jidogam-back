package region.jidogam.domain.guidebook.dto;

import java.util.UUID;

public record AreaRatioDto(
    UUID areaId,
    Long placeCount,
    Double ratio
) {

  public AreaRatioDto(UUID areaId, Long placeCount) {
    this(areaId, placeCount, null);
  }
}
