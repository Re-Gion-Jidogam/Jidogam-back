package region.jidogam.domain.guidebook.repository.querydsl;

import static region.jidogam.domain.guidebook.entity.QGuidebookReview.guidebookReview;
import static region.jidogam.domain.user.entity.QUser.user;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.GuidebookReviewCursor;
import region.jidogam.domain.guidebook.entity.GuidebookReview;

@Repository
@RequiredArgsConstructor
public class GuidebookReviewRepositoryCustomImpl implements GuidebookReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<GuidebookReview> searchByGuidebookId(
      UUID guidebookId,
      GuidebookReviewCursor cursor,
      SortDirection direction,
      int limit
  ) {
    return queryFactory
        .selectFrom(guidebookReview)
        .join(guidebookReview.author, user).fetchJoin()
        .where(
            guidebookReview.guidebook.id.eq(guidebookId),
            guidebookReview.deletedAt.isNull(),
            buildCursorCondition(cursor, direction)
        )
        .orderBy(buildOrder(direction))
        .limit(limit)
        .fetch();
  }

  @Override
  public long countByGuidebookId(UUID guidebookId) {
    Long count = queryFactory
        .select(guidebookReview.count())
        .from(guidebookReview)
        .where(
            guidebookReview.guidebook.id.eq(guidebookId),
            guidebookReview.deletedAt.isNull()
        )
        .fetchOne();
    return count != null ? count : 0L;
  }

  private com.querydsl.core.types.dsl.BooleanExpression buildCursorCondition(
      GuidebookReviewCursor cursor, SortDirection direction) {

    if (cursor == null || cursor.createdAt() == null || cursor.lastId() == null) {
      return null;
    }

    if (direction == SortDirection.ASC) {
      return guidebookReview.createdAt.gt(cursor.createdAt())
          .or(guidebookReview.createdAt.eq(cursor.createdAt())
              .and(guidebookReview.id.gt(cursor.lastId())));
    }

    return guidebookReview.createdAt.lt(cursor.createdAt())
        .or(guidebookReview.createdAt.eq(cursor.createdAt())
            .and(guidebookReview.id.lt(cursor.lastId())));
  }

  private OrderSpecifier<?>[] buildOrder(SortDirection direction) {
    if (direction == SortDirection.ASC) {
      return new OrderSpecifier[]{
          guidebookReview.createdAt.asc(),
          guidebookReview.id.asc()
      };
    }
    return new OrderSpecifier[]{
        guidebookReview.createdAt.desc(),
        guidebookReview.id.desc()
    };
  }
}