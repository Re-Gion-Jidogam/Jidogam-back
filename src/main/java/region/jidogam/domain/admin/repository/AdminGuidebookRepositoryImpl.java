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
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.QGuidebook;
import region.jidogam.domain.user.entity.QUser;

@Repository
@RequiredArgsConstructor
public class AdminGuidebookRepositoryImpl implements AdminGuidebookRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Guidebook> searchGuidebooks(String keyword, Boolean isPublished, Pageable pageable) {
    QGuidebook guidebook = QGuidebook.guidebook;
    QUser author = QUser.user;

    BooleanBuilder builder = new BooleanBuilder();

    if (keyword != null && !keyword.isBlank()) {
      builder.and(
          guidebook.title.containsIgnoreCase(keyword)
              .or(guidebook.author.nickname.containsIgnoreCase(keyword))
      );
    }

    if (isPublished != null) {
      builder.and(guidebook.isPublished.eq(isPublished));
    }

    List<Guidebook> content = queryFactory.selectFrom(guidebook)
        .join(guidebook.author, author).fetchJoin()
        .where(builder)
        .orderBy(guidebook.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory.select(guidebook.count())
        .from(guidebook)
        .where(builder);

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }
}
