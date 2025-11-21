package region.jidogam.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.ParticipationFilter;

@ParameterObject
public record GuidebookParticipationSearchRequest(
    @Schema(description = "검색 키워드", example = "밥")
    String keyword,

    @Schema(description = "정렬 기준(LAST_ACTIVITY_AT)", defaultValue = "LAST_ACTIVITY_AT", nullable = true)
    GuidebookParticipationSortBy sortBy,

    @Schema(description = "정렬 방향(ASC, DESC)", example = "DESC")
    SortDirection sortDirection,

    @Schema(description = "커서")
    String cursor,

    @Schema(description = "조회 크기(개수)", example = "20")
    @Max(value = 100)
    @Min(value = 1)
    Integer limit,

    @Schema(description = "필터 타입(COMPLETED, PROGRESS)", nullable = true)
    ParticipationFilter filter
) {

  public GuidebookParticipationSearchRequest {
    limit = limit == null ? 20 : limit;
    sortBy = sortBy == null ? GuidebookParticipationSortBy.LAST_ACTIVITY_AT : sortBy;
    sortDirection = sortDirection == null ? SortDirection.DESC : sortDirection;
  }
}
