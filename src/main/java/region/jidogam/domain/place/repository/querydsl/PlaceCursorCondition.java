package region.jidogam.domain.place.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import java.util.UUID;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.place.dto.PlaceCursor;
import region.jidogam.domain.place.dto.PlaceSortBy;
import region.jidogam.domain.place.entity.QPlace;

/**
 * 장소 커서 기반 페이지네이션 조건 생성 유틸리티.
 */
public class PlaceCursorCondition {

  private static final QPlace place = QPlace.place;

  /**
   * 숫자 기반 커서 조건 생성
   */
  public static BooleanExpression buildCountCursor(
      Integer cursorCount,
      UUID lastId,
      NumberPath<Integer> countField,
      SortDirection direction) {

    if (cursorCount == null || lastId == null) {
      return null;
    }

    if (direction == SortDirection.ASC) {
      return countField.gt(cursorCount)
          .or(countField.eq(cursorCount)
              .and(place.id.gt(lastId)));
    }

    return countField.lt(cursorCount)
        .or(countField.eq(cursorCount)
            .and(place.id.lt(lastId)));
  }

  /**
   * 장소 커서 조건 (STAMP_COUNT)
   * <p>
   * 주의: 거리순 정렬은 따로 관리합니다.
   */
  public static BooleanExpression buildPlaceCursor(
      PlaceCursor cursor,
      PlaceSortBy sortBy,
      SortDirection direction
  ) {

    if (cursor == null) {
      return null;
    }

    return switch (sortBy) {
      case STAMP_COUNT -> buildCountCursor(
          cursor.stampCount(),
          cursor.lastId(),
          place.stampCount,
          direction
      );
      default -> throw new IllegalArgumentException("Unsupported sort type: " + sortBy);
    };
  }
}
