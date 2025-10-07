package region.jidogam.domain.guidebook.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.QGuidebook;
import region.jidogam.domain.user.entity.QUser;
import region.jidogam.domain.user.entity.User;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GuidebookRepositoryImpl implements GuidebookRepositoryCustom{

  private final JPAQueryFactory queryFactory;

  private final QGuidebook qGuidebook = QGuidebook.guidebook;
  private final QUser qUser = QUser.user;

  @Override
  public List<Guidebook> findPublicGuidebooksByAuthor(User author,
      String sortBy, String sortDirection, String cursor, Integer size, String keyword,
      String filter) {

    BooleanBuilder where = new BooleanBuilder();
    where.and(qUser.id.eq(author.getId()));
    where.and(qGuidebook.isPublished.isTrue());
    where.and(applySearchKeywordCondition(keyword));


    // 커서 조건
    if(sortBy.equalsIgnoreCase("updatedAt")){
      applyUpdatedAtCursor(where, LocalDateTime.parse(cursor), sortDirection.equalsIgnoreCase("desc"));
    }

    OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifier(sortBy, sortDirection);

    return queryFactory.selectFrom(qGuidebook)
        .where(where)
        .leftJoin(qGuidebook.author, qUser)
        .where(where.and(qUser.id.eq(author.getId())))
        .orderBy(orderSpecifiers)
        .limit(size)
        .fetch();
  }


  // 키워드 검색 조건 생성
  private BooleanBuilder applySearchKeywordCondition(String searchKeyword) {
    BooleanBuilder whereClause = new BooleanBuilder();

    if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
      return whereClause;
    }
    // 검색어 조건
    String likePattern = "%" + searchKeyword + "%";

    // 가이드북 제목
    BooleanExpression guidebookTitleCondition = qGuidebook.title.toLowerCase().like(likePattern);

    return whereClause.and(guidebookTitleCondition);
  }

  //정렬 조건 생성
  private OrderSpecifier<?>[] getOrderSpecifier(String sortBy, String sortDirection) {
    boolean isDesc = sortDirection == null || sortDirection.equalsIgnoreCase("desc");
    String lowerSortBy = sortBy == null ? "createdat" : sortBy.toLowerCase();

    OrderSpecifier<?> primarySort = switch (lowerSortBy) {
      case "updatedat" -> isDesc ? qGuidebook.updatedAt.desc() : qGuidebook.updatedAt.asc();
      default -> isDesc ? qGuidebook.createdAt.desc() : qGuidebook.createdAt.asc();
    };

    OrderSpecifier<?> secondarySort = isDesc ? qGuidebook.id.desc() : qGuidebook.id.asc();

    return new OrderSpecifier[] { primarySort, secondarySort };
  }

  //생성일 커서 적용
  private void applyCreatedAtCursor(BooleanBuilder whereClause, LocalDateTime cursorCreatedAt,
      UUID lastId, boolean isDesc) {

    if (isDesc) {
      whereClause.and(
          //커서 날짜보다 이전
          qGuidebook.createdAt.lt(cursorCreatedAt)
              //커서 날짜와 같지만 아이디가 적은 것
              .or(qGuidebook.createdAt.eq(cursorCreatedAt).and(qGuidebook.id.lt(lastId)))
      );
      return;
    }
    whereClause.and(
        qGuidebook.createdAt.gt(cursorCreatedAt)
            .or(qGuidebook.createdAt.eq(cursorCreatedAt).and(qGuidebook.id.gt(lastId)))
    );
  }

  //수정일 커서 적용
  private void applyUpdatedAtCursor(BooleanBuilder whereClause, LocalDateTime cursorUpdatedAt,
      UUID lastId, boolean isDesc) {

    if (isDesc) {
      whereClause.and(
          //커서 날짜보다 이전
          qGuidebook.updatedAt.lt(cursorCreatedAt)
              //커서 날짜와 같지만 아이디가 적은 것
              .or(qGuidebook.updatedAt.eq(cursorCreatedAt).and(qGuidebook.id.lt(lastId)))
      );
      return;
    }
    whereClause.and(
        qGuidebook.updatedAt.gt(cursorCreatedAt)
            .or(qGuidebook.updatedAt.eq(cursorCreatedAt).and(qGuidebook.id.gt(lastId)))
    );
  }
}
