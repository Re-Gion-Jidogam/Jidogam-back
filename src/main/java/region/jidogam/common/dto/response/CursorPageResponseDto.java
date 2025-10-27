package region.jidogam.common.dto.response;

import java.util.List;
import lombok.Builder;
import region.jidogam.common.dto.SortDirection;

@Builder
public record CursorPageResponseDto<T>(
  List<T> data,
  String nextCursor,
  int size,
  boolean hasNext,
  String sortBy,
  SortDirection sortDirection,
  long totalCount
) {

}
