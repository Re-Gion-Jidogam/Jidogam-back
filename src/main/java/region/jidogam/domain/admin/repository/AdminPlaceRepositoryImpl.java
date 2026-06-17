package region.jidogam.domain.admin.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import region.jidogam.domain.area.entity.QArea;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.entity.QPlace;

@Repository
@RequiredArgsConstructor
public class AdminPlaceRepositoryImpl implements AdminPlaceRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Place> searchPlaces(String keyword, Boolean deleted, Pageable pageable) {
    QPlace place = QPlace.place;
    QArea area = QArea.area;

    BooleanBuilder builder = new BooleanBuilder();

    if (keyword != null && !keyword.isBlank()) {
      builder.and(
          place.name.containsIgnoreCase(keyword)
              .or(place.address.containsIgnoreCase(keyword))
              .or(place.kakaoId.containsIgnoreCase(keyword))
      );
    }

    if (deleted != null) {
      if (deleted) {
        builder.and(place.deletedAt.isNotNull());
      } else {
        builder.and(place.deletedAt.isNull());
      }
    }

    List<Place> content = queryFactory.selectFrom(place)
        .join(place.area, area).fetchJoin()
        .where(builder)
        .orderBy(place.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory.select(place.count())
        .from(place)
        .where(builder);

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }
}
