package region.jidogam.domain.stamp.repository.querydsl;

import static region.jidogam.domain.place.entity.QPlace.place;
import static region.jidogam.domain.stamp.entity.QStamp.stamp;
import static region.jidogam.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.stamp.dto.StampCursor;
import region.jidogam.domain.stamp.dto.StampSortBy;
import region.jidogam.domain.stamp.entity.Stamp;

@Repository
@RequiredArgsConstructor
public class StampRepositoryCustomImpl implements StampRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Stamp> searchStampsByUserId(
      UUID userId,
      StampCursor cursor,
      String keyword,
      StampSortBy sortBy,
      SortDirection direction,
      int limit
  ) {
    BooleanBuilder where = new BooleanBuilder();

    // 필수 조건: 사용자 ID
    where.and(StampCondition.userIdEq(userId));

    // 선택 조건: 장소 이름 검색
    where.and(StampCondition.placeNameContains(keyword));

    // 커서 조건
    where.and(StampCursorCondition.buildStampCursor(cursor, sortBy, direction));

    return queryFactory
        .selectFrom(stamp)
        .leftJoin(stamp.user, user).fetchJoin()
        .leftJoin(stamp.place, place).fetchJoin()
        .where(where)
        .orderBy(StampOrderBuilder.forStamp(sortBy, direction))
        .limit(limit)
        .fetch();
  }

  @Override
  public long countStampsByUserId(UUID userId, String keyword) {
    BooleanBuilder where = new BooleanBuilder();

    // 필수 조건: 사용자 ID
    where.and(StampCondition.userIdEq(userId));

    // 선택 조건: 장소 이름 검색
    where.and(StampCondition.placeNameContains(keyword));

    Long count = queryFactory
        .select(stamp.count())
        .from(stamp)
        .leftJoin(stamp.place, place)
        .where(where)
        .fetchOne();

    return count != null ? count : 0L;
  }
}
