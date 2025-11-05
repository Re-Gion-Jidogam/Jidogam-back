package region.jidogam.domain.place.repository.querydsl;

import static region.jidogam.domain.place.entity.QPlace.place;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import region.jidogam.common.dto.SortDirection;

public class PlaceOrderBuilder {

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
        ? place.id.asc()
        : place.id.desc();
  }
}
