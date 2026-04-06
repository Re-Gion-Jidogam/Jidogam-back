package region.jidogam.domain.guidebook.repository.querydsl;

import static region.jidogam.domain.guidebook.entity.QGuidebook.guidebook;
import static region.jidogam.domain.guidebook.entity.QGuidebookParticipation.guidebookParticipation;
import static region.jidogam.domain.user.entity.QUser.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.ParticipationFilter;
import region.jidogam.domain.guidebook.entity.GuidebookParticipation;
import region.jidogam.domain.user.dto.GuidebookParticipationCursor;

@Repository
@RequiredArgsConstructor
public class GuidebookParticipationRepositoryCustomImpl implements
    GuidebookParticipationRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<GuidebookParticipation> searchParticipatingGuidebooks(
      UUID userId,
      GuidebookParticipationCursor cursor,
      String keyword,
      SortDirection sortDirection,
      ParticipationFilter filter,
      int limit
  ) {

    return queryFactory
        .selectFrom(guidebookParticipation)
        .leftJoin(guidebookParticipation.guidebook, guidebook).fetchJoin()
        .leftJoin(guidebook.author, user).fetchJoin()
        .where(
            guidebookParticipation.user.id.eq(userId), // 참여자 ID
            GuidebookParticipationCondition.titleContains(keyword), // 키워드 제목 검색
            GuidebookParticipationCondition.isCompletedFilter(filter), // 완료 여부 필터
            GuidebookParticipationCursorCondition.buildGuidebookParticipationCursor(cursor,
            sortDirection)) // 커서 조건
        .orderBy(GuidebookParticipationOrderBuilder.forParticipantGuidebook(sortDirection))
        .limit(limit)
        .fetch();
  }

  @Override
  public long countParticipatingGuidebooks(UUID userId, String keyword, ParticipationFilter filter) {

    Long count = queryFactory
        .select(guidebookParticipation.count())
        .from(guidebookParticipation)
        .leftJoin(guidebookParticipation.guidebook, guidebook)
        .where(guidebookParticipation.user.id.eq(userId), // 참여자 ID
            GuidebookParticipationCondition.titleContains(keyword), // 키워드
            GuidebookParticipationCondition.isCompletedFilter(filter)) // 완료 여부
        .fetchOne();

    return count != null ? count : 0L;
  }
}
