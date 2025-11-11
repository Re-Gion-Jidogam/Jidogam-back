package region.jidogam.domain.stamp.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import java.time.LocalDateTime;
import java.util.UUID;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.stamp.dto.StampCursor;
import region.jidogam.domain.stamp.dto.StampSortBy;
import region.jidogam.domain.stamp.entity.QStamp;

public class StampCursorCondition {

  private static final QStamp stamp = QStamp.stamp;

  // 날짜 기반 커서 조건 생성 (createdAt)
  public static BooleanExpression buildDateCursor(
      LocalDateTime cursorDate,
      UUID lastId,
      DateTimePath<LocalDateTime> dateField,
      SortDirection direction) {

    if (cursorDate == null || lastId == null) {
      return null;
    }

    if (direction == SortDirection.ASC) {
      return dateField.gt(cursorDate)
          .or(dateField.eq(cursorDate)
              .and(stamp.id.gt(lastId)));
    }

    return dateField.lt(cursorDate)
        .or(dateField.eq(cursorDate)
            .and(stamp.id.lt(lastId)));
  }

  // 스탬프 커서 조건 (CREATED_AT)
  public static BooleanExpression buildStampCursor(
      StampCursor cursor,
      StampSortBy sortBy,
      SortDirection direction) {

    if (cursor == null) {
      return null;
    }

    return switch (sortBy) {
      case CREATED_AT -> buildDateCursor(
          cursor.createdAt(),
          cursor.lastId(),
          stamp.createdAt,
          direction
      );
    };
  }
}
