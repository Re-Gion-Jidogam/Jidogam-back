package region.jidogam.domain.guidebook.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import java.time.LocalDateTime;
import java.util.UUID;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.entity.QGuidebookParticipant;
import region.jidogam.domain.user.dto.GuidebookParticipationCursor;

public class GuidebookParticipationCursorCondition {

  private static final QGuidebookParticipant guidebookParticipant = QGuidebookParticipant.guidebookParticipant;

  /**
   * 날짜 기반 커서 조건 생성 (lastActivityAt 등)
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
              .and(guidebookParticipant.id.gt(lastId)));
    }

    return dateField.lt(cursorDate)
        .or(dateField.eq(cursorDate)
            .and(guidebookParticipant.id.lt(lastId)));
  }

  /**
   * 참여 가이드북 커서 조건 (LAST_ACTIVITY_AT)
   */
  public static BooleanExpression buildParticipantGuidebookCursor(
      GuidebookParticipationCursor cursor,
      SortDirection direction) {

    if (cursor == null) {
      return null;
    }

    return buildDateCursor(
        cursor.lastActivityAt(),
        cursor.lastId(),
        guidebookParticipant.lastActivityAt,
        direction
    );
  }
}
