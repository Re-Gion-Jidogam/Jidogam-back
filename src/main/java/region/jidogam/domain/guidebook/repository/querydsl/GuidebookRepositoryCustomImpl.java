package region.jidogam.domain.guidebook.repository.querydsl;

import static region.jidogam.domain.guidebook.entity.QGuidebook.guidebook;
import static region.jidogam.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.GuidebookCursor;
import region.jidogam.domain.guidebook.dto.GuidebookSortBy;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.user.dto.UserGuideBookSortBy;
import region.jidogam.domain.user.dto.UserGuidebookCursor;

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
      int limit
  ) {

    JPAQuery<Guidebook> query = queryFactory
        .selectFrom(guidebook)
        .leftJoin(guidebook.author, user).fetchJoin();

    BooleanBuilder where = new BooleanBuilder();

    where.and(GuidebookCondition.isPublished());
    where.and(GuidebookCondition.titleContains(keyword));
    where.and(GuidebookCursorCondition.buildGuidebookCursor(cursor, sortBy, direction));

    query.where(where);

    query.orderBy(GuidebookOrderBuilder.forGuidebook(sortBy, direction));

    query.limit(limit);

    return query.fetch();
  }

  @Override
  public List<Guidebook> searchGuidebookByAuthorId(UUID authorId, UserGuidebookCursor cursor,
      String keyword, UserGuideBookSortBy sortBy, SortDirection direction, int limit) {

    //Select 결과 DTO로 받는게 더 빠르고 효율적이라는 이야기가 있음 - 추후 검증/논의 후 수정
    return queryFactory.selectFrom(guidebook)
        .where(
            guidebook.author.id.eq(authorId),
            GuidebookCondition.titleContains(keyword),
            GuidebookCursorCondition.buildUserGuidebookCursor(cursor, sortBy, direction)
        )
        .leftJoin(guidebook.author, user)
        .orderBy(GuidebookOrderBuilder.forUserGuidebook(sortBy, direction))
        .limit(limit)
        .fetch();
  }
}
