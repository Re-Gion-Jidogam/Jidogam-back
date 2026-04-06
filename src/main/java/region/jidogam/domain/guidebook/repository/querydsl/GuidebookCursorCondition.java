package region.jidogam.domain.guidebook.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import java.time.LocalDateTime;
import java.util.UUID;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.GuidebookCursor;
import region.jidogam.domain.guidebook.dto.GuidebookSortBy;
import region.jidogam.domain.guidebook.entity.QGuidebook;
import region.jidogam.domain.user.dto.UserGuideBookSortBy;
import region.jidogam.domain.user.dto.UserGuidebookCursor;

public class GuidebookCursorCondition {

  private static final QGuidebook guidebook = QGuidebook.guidebook;

  /**
   * 날짜 기반 커서 조건 생성 (createdAt, updatedAt 등)
   */
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
              .and(guidebook.id.gt(lastId)));
    }

    return dateField.lt(cursorDate)
        .or(dateField.eq(cursorDate)
            .and(guidebook.id.lt(lastId)));
  }

  /**
   * 숫자 기반 커서 조건 생성 (participantCount 등)
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
              .and(guidebook.id.gt(lastId)));
    }

    return countField.lt(cursorCount)
        .or(countField.eq(cursorCount)
            .and(guidebook.id.lt(lastId)));
  }

  /**
   * 일반 가이드북 커서 조건 (CREATED_AT, PARTICIPANT_COUNT)
   */
  public static BooleanExpression buildGuidebookCursor(
      GuidebookCursor cursor,
      GuidebookSortBy sortBy,
      SortDirection direction) {

    if (cursor == null) {
      return null;
    }

    return switch (sortBy) {
      case CREATED_AT -> buildDateCursor(
          cursor.createdAt(),
          cursor.lastId(),
          guidebook.createdAt,
          direction
      );
      case PARTICIPANT_COUNT -> buildCountCursor(
          cursor.participantCount(),
          cursor.lastId(),
          guidebook.participantCount,
          direction
      );
    };
  }

  /**
   * 사용자 가이드북 커서 조건 (CREATED_AT, UPDATED_AT, PARTICIPANT_COUNT)
   */
  public static BooleanExpression buildUserGuidebookCursor(
      UserGuidebookCursor cursor,
      UserGuideBookSortBy sortBy,
      SortDirection direction) {

    if (cursor == null) {
      return null;
    }

    return switch (sortBy) {
      case CREATED_AT -> buildDateCursor(
          cursor.date(),
          cursor.lastId(),
          guidebook.createdAt,
          direction
      );
      default -> buildDateCursor(
          cursor.date(),
          cursor.lastId(),
          guidebook.updatedAt,
          direction
      );
    };
  }
}