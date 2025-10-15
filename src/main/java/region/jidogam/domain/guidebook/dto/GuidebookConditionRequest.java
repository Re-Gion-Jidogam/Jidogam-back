package region.jidogam.domain.guidebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import region.jidogam.common.dto.SortDirection;

@ParameterObject
public record GuidebookConditionRequest(

  @Schema(description = "필터 타입", nullable = true)
  GuidebookFilter filter,

  @Schema(description = "정렬 기준", defaultValue = "CREATED_AT", nullable = true)
  GuidebookSortBy sortBy,

  @Schema(description = "정렬 방향", defaultValue = "desc", nullable = true)
  SortDirection sortDirection,

  @Schema(description = "커서 값")
  String cursor,

  @Min(1)
  @Max(100)
  @Schema(description = "페이지 크기", defaultValue = "20", minimum = "1", maximum = "100")
  Integer limit,

  @Schema(description = "검색 키워드", maxLength = 50)
  String keyword

) {

  // 기본값 설정
  public GuidebookConditionRequest {
    if (limit == null) {
      limit = 20;
    }
    if (sortBy == null) {
      sortBy = GuidebookSortBy.CREATED_AT;
    }
    if (sortDirection == null) {
      sortDirection = SortDirection.DESC;
    }
  }

}
