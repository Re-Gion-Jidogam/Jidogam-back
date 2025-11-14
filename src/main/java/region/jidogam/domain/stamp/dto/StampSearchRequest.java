package region.jidogam.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import region.jidogam.common.dto.SortDirection;

@ParameterObject
public record StampSearchRequest(

    @Schema(description = "정렬 기준", defaultValue = "createdAt", nullable = true)
    StampSortBy sortBy,

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
  public StampSearchRequest {
    if (limit == null) {
      limit = 20;
    }
    if (sortBy == null) {
      sortBy = StampSortBy.CREATED_AT;
    }
    if (sortDirection == null) {
      sortDirection = SortDirection.DESC;
    }
  }
}
