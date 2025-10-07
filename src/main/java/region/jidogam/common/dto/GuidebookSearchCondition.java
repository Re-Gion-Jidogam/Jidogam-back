package region.jidogam.common.dto;

import lombok.Builder;
import region.jidogam.domain.user.dto.UserGuidebookSearchRequest;

@Builder
public record GuidebookSearchCondition(
    String searchKeyword,
    String sortBy,
    String sortDirection,
    Cursor cursor,
    Integer size
) {
  public static GuidebookSearchCondition fromRequestAndCursor(UserGuidebookSearchRequest request, Cursor cursor) {
    return GuidebookSearchCondition.builder()
        .searchKeyword(request.searchKeyword())
        .sortBy(request.sortBy())
        .sortDirection(request.sortDirection())
        .cursor(cursor)
        .size(request.size())
        .build();

  }
}
