package region.jidogam.domain.guidebook.repository.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.entity.QGuidebookParticipant;

public class GuidebookParticipationOrderBuilder {

  private static final QGuidebookParticipant guidebookParticipant = QGuidebookParticipant.guidebookParticipant;

  /**
   * 참여 가이드북 정렬 (LAST_ACTIVITY_AT)
   */
  public static OrderSpecifier<?>[] forParticipantGuidebook(SortDirection direction) {
    OrderSpecifier<?> primaryOrder = buildOrder(guidebookParticipant.lastActivityAt, direction);
    OrderSpecifier<?> secondaryOrder = buildIdOrder(direction);

    return new OrderSpecifier<?>[]{primaryOrder, secondaryOrder};
  }

  /**
   * 필드에 대한 정렬 생성
   */
  private static <T extends Comparable<?>> OrderSpecifier<T> buildOrder(
      ComparableExpressionBase<T> field,
      SortDirection direction) {

    return direction == SortDirection.ASC
        ? field.asc()
        : field.desc();
  }

  /**
   * ID 보조 정렬
   */
  private static OrderSpecifier<?> buildIdOrder(SortDirection direction) {
    return direction == SortDirection.ASC
        ? guidebookParticipant.id.asc()
        : guidebookParticipant.id.desc();
  }
}
