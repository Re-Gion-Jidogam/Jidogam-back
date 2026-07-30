package region.jidogam.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record PlaceStoreInitRequest(

    @Schema(description = "적재할 시군구코드 목록", example = "[\"41210\"]")
    @NotEmpty(message = "시군구코드 목록은 필수입니다.")
    List<String> sigunguCodes,

    @Schema(description = "시군구당 가져올 최대 건수", example = "1000")
    @Positive(message = "limit은 1 이상이어야 합니다.")
    int limit,

    @Schema(description = "업종 대분류코드로 필터링", example = "G2", nullable = true)
    String largeCategoryCode,

    @Schema(description = "업종 중분류코드로 필터링", example = "G204", nullable = true)
    String mediumCategoryCode,

    @Schema(description = "업종 소분류코드로 필터링", nullable = true)
    String smallCategoryCode
) {

}
