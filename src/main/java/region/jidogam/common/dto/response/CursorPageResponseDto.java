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
    Long totalCount
) {

  public CursorPageResponseDto<T> withTotalCount(long totalCount) {
    return new CursorPageResponseDto<>(
        this.data,
        this.nextCursor,
        this.size,
        this.hasNext,
        this.sortBy,
        this.sortDirection,
        totalCount
    );
  }
}
