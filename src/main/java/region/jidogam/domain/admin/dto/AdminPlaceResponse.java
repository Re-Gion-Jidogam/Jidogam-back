package region.jidogam.domain.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import region.jidogam.domain.place.entity.Place;

@Builder
public record AdminPlaceResponse(
    UUID id,
    String kakaoId,
    String name,
    String address,
    String category,
    String areaName,
    BigDecimal x,
    BigDecimal y,
    Integer exp,
    Integer guidebookCount,
    Integer stampCount,
    LocalDateTime createdAt,
    LocalDateTime deletedAt,
    boolean deleted
) {

  public static AdminPlaceResponse from(Place place) {
    return AdminPlaceResponse.builder()
        .id(place.getId())
        .kakaoId(place.getKakaoId())
        .name(place.getName())
        .address(place.getAddress())
        .category(place.getCategory())
        .areaName(place.getArea() != null ? place.getArea().areaName() : null)
        .x(place.getX())
        .y(place.getY())
        .exp(place.getExp())
        .guidebookCount(place.getGuidebookCount())
        .stampCount(place.getStampCount())
        .createdAt(place.getCreatedAt())
        .deletedAt(place.getDeletedAt())
        .deleted(place.isDeleted())
        .build();
  }
}
