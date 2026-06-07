package region.jidogam.domain.guidebook.repository.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.entity.QGuidebookReview;

public class GuidebookReviewOrderBuilder {

  private static final QGuidebookReview guidebookReview = QGuidebookReview.guidebookReview;

  public static OrderSpecifier<?>[] forReview(SortDirection direction) {
    return new OrderSpecifier<?>[]{
        buildOrder(guidebookReview.createdAt, direction),
        buildIdOrder(direction)
    };
  }

  private static <T extends Comparable<?>> OrderSpecifier<T> buildOrder(
      ComparableExpressionBase<T> field,
      SortDirection direction) {

    return direction == SortDirection.ASC
        ? field.asc()
        : field.desc();
  }

  private static OrderSpecifier<?> buildIdOrder(SortDirection direction) {
    return direction == SortDirection.ASC
        ? guidebookReview.id.asc()
        : guidebookReview.id.desc();
  }
}
