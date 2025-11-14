package region.jidogam.domain.stamp.repository.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.stamp.dto.StampSortBy;
import region.jidogam.domain.stamp.entity.QStamp;

public class StampOrderBuilder {

  private static final QStamp stamp = QStamp.stamp;

  // 스탬프 정렬 (CREATED_AT)
  public static OrderSpecifier<?>[] forStamp(
      StampSortBy sortBy,
      SortDirection direction) {

    OrderSpecifier<?> primaryOrder = switch (sortBy) {
      case CREATED_AT -> buildOrder(stamp.createdAt, direction);
    };

    OrderSpecifier<?> secondaryOrder = buildIdOrder(direction);

    return new OrderSpecifier<?>[] { primaryOrder, secondaryOrder };
  }

  // 필드에 대한 정렬 생성
  private static <T extends Comparable<?>> OrderSpecifier<T> buildOrder(
      ComparableExpressionBase<T> field,
      SortDirection direction) {

    return direction == SortDirection.ASC
        ? field.asc()
        : field.desc();
  }

  // Place ID 보조 정렬
  private static OrderSpecifier<?> buildIdOrder(SortDirection direction) {
    return direction == SortDirection.ASC
        ? stamp.place.id.asc()
        : stamp.place.id.desc();
  }
}
