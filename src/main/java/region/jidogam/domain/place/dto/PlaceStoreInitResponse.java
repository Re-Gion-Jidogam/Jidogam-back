package region.jidogam.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record PlaceStoreInitResponse(

    @Schema(description = "시군구별 적재 결과")
    List<PlaceStoreInitResult> results,

    @Schema(description = "전체 적재 시도 건수", example = "3000")
    int totalAttemptedCount,

    @Schema(description = "전체 적재 성공 건수", example = "2990")
    int totalSucceededCount,

    @Schema(description = "전체 적재 실패 건수", example = "10")
    int totalFailedCount
) {

  public static PlaceStoreInitResponse of(List<PlaceStoreInitResult> results) {
    int totalAttempted = results.stream().mapToInt(PlaceStoreInitResult::attemptedCount).sum();
    int totalSucceeded = results.stream().mapToInt(PlaceStoreInitResult::succeededCount).sum();
    int totalFailed = results.stream().mapToInt(PlaceStoreInitResult::failedCount).sum();

    return PlaceStoreInitResponse.builder()
        .results(results)
        .totalAttemptedCount(totalAttempted)
        .totalSucceededCount(totalSucceeded)
        .totalFailedCount(totalFailed)
        .build();
  }
}
