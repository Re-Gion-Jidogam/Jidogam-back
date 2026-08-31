package region.jidogam.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PlaceStoreInitResult(

    @Schema(description = "시군구코드", example = "41210")
    String sigunguCode,

    @Schema(description = "적재 시도 건수", example = "1000")
    int attemptedCount,

    @Schema(description = "적재 성공 건수", example = "998")
    int succeededCount,

    @Schema(description = "적재 실패 건수", example = "2")
    int failedCount
) {

}
