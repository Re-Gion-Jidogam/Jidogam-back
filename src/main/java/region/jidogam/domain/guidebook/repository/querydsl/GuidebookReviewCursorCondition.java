package region.jidogam.domain.guidebook.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.GuidebookReviewCursor;
import region.jidogam.domain.guidebook.entity.QGuidebookReview;

public class GuidebookReviewCursorCondition {

  private static final QGuidebookReview guidebookReview = QGuidebookReview.guidebookReview;

  public static BooleanExpression buildDateCursor(
      GuidebookReviewCursor cursor, SortDirection direction) {

    if (cursor.createdAt() == null || cursor.lastId() == null) {
      return null;
    }

    if (direction == SortDirection.ASC) {
      return guidebookReview.createdAt.gt(cursor.createdAt())
          .or(guidebookReview.createdAt.eq(cursor.createdAt())
              .and(guidebookReview.id.gt(cursor.lastId())));
    }

    return guidebookReview.createdAt.lt(cursor.createdAt())
        .or(guidebookReview.createdAt.eq(cursor.createdAt())
            .and(guidebookReview.id.lt(cursor.lastId())));
  }
}
