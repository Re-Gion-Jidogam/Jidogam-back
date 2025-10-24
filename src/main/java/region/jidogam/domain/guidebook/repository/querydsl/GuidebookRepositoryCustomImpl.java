package region.jidogam.domain.guidebook.repository.querydsl;

import static region.jidogam.domain.guidebook.entity.QGuidebook.guidebook;
import static region.jidogam.domain.guidebook.entity.QGuidebookAreaRatio.guidebookAreaRatio;
import static region.jidogam.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.area.entity.QArea;
import region.jidogam.domain.guidebook.dto.GuidebookCursor;
import region.jidogam.domain.guidebook.dto.GuidebookSortBy;
import region.jidogam.domain.guidebook.entity.Guidebook;

@Repository
@RequiredArgsConstructor
public class GuidebookRepositoryCustomImpl implements GuidebookRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Guidebook> searchGuidebook(
      GuidebookCursor cursor,
      String keyword,
      GuidebookSortBy sortBy,
      SortDirection direction,
      Boolean isLocal,
      int limit
  ) {
    QArea firstArea = new QArea("firstArea");
    QArea secondArea = new QArea("secondArea");
    QArea thirdArea = new QArea("thirdArea");

    JPAQuery<Guidebook> query = queryFactory
        .selectFrom(guidebook)
        .leftJoin(guidebook.author, user).fetchJoin()
        .leftJoin(guidebook.areaRatio, guidebookAreaRatio).fetchJoin()
        .leftJoin(guidebookAreaRatio.firstArea, firstArea).fetchJoin()
        .leftJoin(guidebookAreaRatio.secondArea, secondArea).fetchJoin()
        .leftJoin(guidebookAreaRatio.thirdArea, thirdArea).fetchJoin();

    BooleanBuilder where = new BooleanBuilder();

    where.and(GuidebookCondition.isPublished());
    where.and(GuidebookCondition.titleContains(keyword));
    where.and(GuidebookCondition.isLocalGuidebook(isLocal));

    if (cursor != null) {
      // PARTICIPANT_COUNT 기준 정렬
      if (sortBy == GuidebookSortBy.PARTICIPANT_COUNT) {
        if (direction == SortDirection.ASC) {
          where.and(
              guidebook.participantCount.gt(cursor.participantCount())
                  .or(guidebook.participantCount.eq(cursor.participantCount())
                      .and(guidebook.id.gt(cursor.lastId())))
          );
        } else {
          where.and(
              guidebook.participantCount.lt(cursor.participantCount())
                  .or(guidebook.participantCount.eq(cursor.participantCount())
                      .and(guidebook.id.lt(cursor.lastId())))
          );
        }
      }
      // CREATED_AT 기준 정렬
      else if (sortBy == GuidebookSortBy.CREATED_AT) {
        if (direction == SortDirection.ASC) {
          where.and(
              guidebook.createdAt.gt(cursor.createdAt())
                  .or(guidebook.createdAt.eq(cursor.createdAt())
                      .and(guidebook.id.gt(cursor.lastId())))
          );
        } else {
          where.and(
              guidebook.createdAt.lt(cursor.createdAt())
                  .or(guidebook.createdAt.eq(cursor.createdAt())
                      .and(guidebook.id.lt(cursor.lastId())))
          );
        }
      }
    }

    query.where(where);

    // 정렬 조건
    OrderSpecifier<?> orderSpecifier;
    OrderSpecifier<?> idOrderSpecifier = guidebook.id.asc(); // 보조 정렬 기준

    if (sortBy == GuidebookSortBy.PARTICIPANT_COUNT) {
      orderSpecifier = direction == SortDirection.ASC
          ? guidebook.participantCount.asc()
          : guidebook.participantCount.desc();
    } else {
      orderSpecifier = direction == SortDirection.ASC
          ? guidebook.createdAt.asc()
          : guidebook.createdAt.desc();
    }

    query.orderBy(orderSpecifier, idOrderSpecifier);

    query.limit(limit);

    return query.fetch();
  }

  @Override
  public long countPublishedGuidebooksByKeyword(String keyword, Boolean isLocal) {
    BooleanBuilder where = new BooleanBuilder();

    where.and(GuidebookCondition.isPublished());
    where.and(GuidebookCondition.titleContains(keyword));
    where.and(GuidebookCondition.isLocalGuidebook(isLocal));

    Long count = queryFactory
        .select(guidebook.count())
        .from(guidebook)
        .where(where)
        .fetchOne();

    return count != null ? count : 0L;
  }
}
