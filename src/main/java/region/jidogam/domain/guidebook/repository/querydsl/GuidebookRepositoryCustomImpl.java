package region.jidogam.domain.guidebook.repository.querydsl;

import static region.jidogam.domain.guidebook.entity.QGuidebook.guidebook;
import static region.jidogam.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import region.jidogam.common.dto.SortDirection;
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
}
