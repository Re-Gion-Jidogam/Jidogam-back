package region.jidogam.domain.place.repository.querydsl;

import static region.jidogam.domain.guidebook.entity.QGuidebookPlace.guidebookPlace;
import static region.jidogam.domain.place.entity.QPlace.place;
import static region.jidogam.domain.stamp.entity.QStamp.stamp;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.place.dto.PlaceCursor;
import region.jidogam.domain.place.dto.PlaceFilter;
import region.jidogam.domain.place.dto.PlaceSortBy;
import region.jidogam.domain.place.entity.Place;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryCustomImpl implements PlaceRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Place> searchPlaceByGuidebook(
      UUID guidebookId,
      UUID userId,
      PlaceFilter filter,
      PlaceCursor cursor,
      PlaceSortBy sortBy,
      SortDirection direction,
      int limit
  ) {

    JPAQuery<Place> query = queryFactory
        .selectFrom(place)
        .join(guidebookPlace).on(guidebookPlace.place.eq(place))
        .where(guidebookPlace.guidebook.id.eq(guidebookId));

    BooleanBuilder where = new BooleanBuilder();

    if (userId != null && filter != null && filter != PlaceFilter.NONE) {
      if (filter == PlaceFilter.VISITED) {
        where.and(JPAExpressions
            .selectOne()
            .from(stamp)
            .where(
                stamp.place.eq(place),
                stamp.user.id.eq(userId)
            )
            .exists());
      } else if (filter == PlaceFilter.NOT_VISITED) {
        where.and(JPAExpressions
            .selectOne()
            .from(stamp)
            .where(
                stamp.place.eq(place),
                stamp.user.id.eq(userId)
            )
            .notExists());
      }
    }

    where.and(PlaceCursorCondition.buildPlaceCursor(cursor, sortBy, direction));

    return query
        .where(where)
        .orderBy(PlaceOrderBuilder.forPlace(sortBy, direction))
        .limit(limit)
        .fetch();
  }
}
