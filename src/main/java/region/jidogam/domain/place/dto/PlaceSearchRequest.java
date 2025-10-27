package region.jidogam.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.place.validaton.ValidCoordinates;

@ParameterObject
@ValidCoordinates
public record PlaceSearchRequest(

    @Schema(description = "위도", example = "35.976749396987046", nullable = true)
    @DecimalMin(value = "-90.0", message = "유효한 위도 값이 아닙니다.")
    @DecimalMax(value = "90.0", message = "유효한 위도 값이 아닙니다.")
    Double lat,

    @Schema(description = "경도", example = "126.99599512792346", nullable = true)
    @DecimalMin(value = "-180.0", message = "유효한 경도 값이 아닙니다.")
    @DecimalMax(value = "180.0", message = "유효한 경도 값이 아닙니다.")
    Double lng,

    @Schema(description = "정렬 기준", defaultValue = "createdAt", nullable = true)
    PlaceSortBy sortBy,

    @Schema(description = "정렬 방향", defaultValue = "desc", nullable = true)
    SortDirection sortDirection,

    @Schema(description = "커서 값", nullable = true)
    String cursor,

    @Min(1)
    @Max(100)
    @Schema(description = "페이지 크기", defaultValue = "20", minimum = "1", maximum = "100")
    Integer limit,

    @Schema(description = "검색 키워드", maxLength = 50, nullable = true)
    String keyword

) {

  // 기본값 설정
  public PlaceSearchRequest {
    if (limit == null) {
      limit = 20;
    }
    if (sortBy == null) {
      sortBy = PlaceSortBy.CREATED_AT;
    }
    if (sortDirection == null) {
      sortDirection = SortDirection.DESC;
    }
  }
}
