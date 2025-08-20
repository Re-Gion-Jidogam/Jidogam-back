package region.jidogam.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PlaceCreateRequest(

  @Schema(description = "카카오 장소ID", example = "")
  String id,

  @Schema(description = "장소명", example = "임시마트")
  @NotBlank(message = "장소명은 필수입니다.")
  String placeName,

  @Schema(description = "주소", example = "전북 익산시 망산길 11-17")
  @NotBlank(message = "주소는 필수입니다.")
  String addressName,

  @Schema(description = "카테고리", example = "마트")
  String category,

  @Schema(description = "위도", example = "35.976749396987046")
  @NotNull(message = "위도 값은 필수입니다.")
  @DecimalMin(value = "-90.0", message = "유효한 위도 값이 아닙니다.")
  @DecimalMax(value = "90.0", message = "유효한 위도 값이 아닙니다.")
  BigDecimal y,

  @Schema(description = "경도", example = "126.99599512792346")
  @NotNull(message = "경도 값은 필수입니다.")
  @DecimalMin(value = "-180.0", message = "유효한 경도 값이 아닙니다.")
  @DecimalMax(value = "180.0", message = "유효한 경도 값이 아닙니다.")
  BigDecimal x
) {

}
