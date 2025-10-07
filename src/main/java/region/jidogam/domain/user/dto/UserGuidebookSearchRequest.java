package region.jidogam.domain.user.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record UserGuidebookSearchRequest(
    @Parameter(description = "검색어", example = "밥")
    String searchKeyword,

    @Parameter(description = "정렬 기준(생성일, 수정일)")
    String sortBy,

    @Parameter(description = "정렬 방향(ASC, DESC)", example = "DESC")
    String sortDirection,

    @Parameter(description = "커서", example = "")
    String cursor,

    @Parameter(description = "조회 크기(개수)", example = "20")
    @Max(value = 100)
    @Min(value = 1)
    Integer size
) {
}
