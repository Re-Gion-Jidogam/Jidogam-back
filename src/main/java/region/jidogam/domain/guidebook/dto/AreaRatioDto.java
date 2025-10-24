package region.jidogam.domain.guidebook.dto;

import region.jidogam.domain.area.entity.Area;

public record AreaRatioDto(
    Area area,
    Long placeCount,
    Double ratio
) {

  public AreaRatioDto(Area area, Long placeCount) {
    this(area, placeCount, null);
  }
}
