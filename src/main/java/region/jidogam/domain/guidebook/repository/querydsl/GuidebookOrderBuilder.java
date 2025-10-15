package region.jidogam.domain.guidebook.repository.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.GuidebookSortBy;
import region.jidogam.domain.guidebook.entity.QGuidebook;
import region.jidogam.domain.user.dto.UserGuideBookSortBy;


public class GuidebookOrderBuilder {

  private static final QGuidebook guidebook = QGuidebook.guidebook;

  /**
   * 일반 가이드북 정렬 (CREATED_AT, PARTICIPANT_COUNT)
   */
  public static OrderSpecifier<?>[] forGuidebook(
      GuidebookSortBy sortBy,
      SortDirection direction) {

    OrderSpecifier<?> primaryOrder = switch (sortBy) {
      case CREATED_AT -> buildOrder(guidebook.createdAt, direction);
      case PARTICIPANT_COUNT -> buildOrder(guidebook.participantCount, direction);
    };

    OrderSpecifier<?> secondaryOrder = buildIdOrder(direction);

    return new OrderSpecifier<?>[] { primaryOrder, secondaryOrder };
  }

  /**
   * 사용자 가이드북 정렬 (CREATED_AT, UPDATED_AT, PARTICIPANT_COUNT)
   */
  public static OrderSpecifier<?>[] forUserGuidebook(
      UserGuideBookSortBy sortBy,
      SortDirection direction) {

    OrderSpecifier<?> primaryOrder = switch (sortBy) {
      case CREATED_AT -> buildOrder(guidebook.createdAt, direction);
      case UPDATED_AT -> buildOrder(guidebook.updatedAt, direction);
      case PARTICIPANT_COUNT -> buildOrder(guidebook.participantCount, direction);
    };

    OrderSpecifier<?> secondaryOrder = buildIdOrder(direction);

    return new OrderSpecifier<?>[] { primaryOrder, secondaryOrder };
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
        ? guidebook.id.asc()
        : guidebook.id.desc();
  }
}