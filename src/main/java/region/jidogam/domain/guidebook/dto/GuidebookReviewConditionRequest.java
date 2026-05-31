package region.jidogam.domain.guidebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import region.jidogam.common.dto.SortDirection;

@ParameterObject
public record GuidebookReviewConditionRequest(

    @Schema(description = "정렬 기준", defaultValue = "createdAt", nullable = true)
    GuidebookReviewSortBy sortBy,

    @Schema(description = "정렬 방향", defaultValue = "desc", nullable = true)
    SortDirection sortDirection,

    @Schema(description = "커서 값", nullable = true)
    String cursor,

    @Min(1)
    @Max(100)
    @Schema(description = "페이지 크기", defaultValue = "20", minimum = "1", maximum = "100")
    Integer limit

) {

  public GuidebookReviewConditionRequest {
    if (sortBy == null) {
      sortBy = GuidebookReviewSortBy.CREATED_AT;
    }
    if (limit == null) {
      limit = 20;
    }
    if (sortDirection == null) {
      sortDirection = SortDirection.DESC;
    }
  }
}