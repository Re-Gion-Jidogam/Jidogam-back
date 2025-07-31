package region.jidogam.common.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record CursorPageResponseDto<T>(
    List<T> data,
    String nextCursor,
    int size,
    long totalCount,
    boolean hasNext,
    String sortBy,
    String sortDirection
) {
}
