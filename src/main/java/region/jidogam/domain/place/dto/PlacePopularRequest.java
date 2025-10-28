package region.jidogam.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import region.jidogam.domain.place.validaton.ValidCoordinates;

@ParameterObject
@ValidCoordinates
public record PlacePopularRequest(
    @Schema(description = "사용자 위도", example = "35.976749396987046", nullable = true)
    @DecimalMin(value = "-90.0", message = "유효한 위도 값이 아닙니다.")
    @DecimalMax(value = "90.0", message = "유효한 위도 값이 아닙니다.")
    Double lat,

    @Schema(description = "사용자 경도", example = "126.99599512792346", nullable = true)
    @DecimalMin(value = "-180.0", message = "유효한 경도 값이 아닙니다.")
    @DecimalMax(value = "180.0", message = "유효한 경도 값이 아닙니다.")
    Double lon,

    @Min(1)
    @Max(100)
    @Schema(description = "페이지 크기", defaultValue = "20", minimum = "1", maximum = "100")
    Integer limit
) {

  // 기본값 설정
  public PlacePopularRequest {
    if (limit == null) {
      limit = 20;
    }
  }

}
