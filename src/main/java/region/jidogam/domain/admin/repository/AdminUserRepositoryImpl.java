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
import region.jidogam.domain.user.entity.QUser;
import region.jidogam.domain.user.entity.User;

@Repository
@RequiredArgsConstructor
public class AdminUserRepositoryImpl implements AdminUserRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<User> searchUsers(String keyword, User.Role role, Boolean deleted,
      Pageable pageable) {
    QUser user = QUser.user;

    BooleanBuilder builder = new BooleanBuilder();

    if (keyword != null && !keyword.isBlank()) {
      builder.and(
          user.email.containsIgnoreCase(keyword)
              .or(user.nickname.containsIgnoreCase(keyword))
      );
    }

    if (role != null) {
      builder.and(user.role.eq(role));
    }

    if (deleted != null) {
      if (deleted) {
        builder.and(user.deletedAt.isNotNull());
      } else {
        builder.and(user.deletedAt.isNull());
      }
    }

    List<User> content = queryFactory.selectFrom(user)
        .where(builder)
        .orderBy(user.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory.select(user.count())
        .from(user)
        .where(builder);

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }
}
