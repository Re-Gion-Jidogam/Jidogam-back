package region.jidogam.domain.guidebook.repository.querydsl;

import static region.jidogam.domain.guidebook.entity.QGuidebook.guidebook;
import static region.jidogam.domain.guidebook.entity.QGuidebookParticipant.guidebookParticipant;
import static region.jidogam.domain.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import region.jidogam.domain.guidebook.dto.GuidebookWithAuthor;

@Repository
@RequiredArgsConstructor
public class GuidebookParticipantRepositoryCustomImpl implements
    GuidebookParticipantRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<GuidebookWithAuthor> searchGuidebooksByParticipantId(UUID userId) {
    return queryFactory.select(
            Projections.constructor(GuidebookWithAuthor.class,
                guidebook,
                user)
        )
        .from(guidebookParticipant)
        .leftJoin(guidebookParticipant.guidebook, guidebook)
        .leftJoin(guidebookParticipant.user, guidebook.author)
        .fetchJoin()
        .where(guidebookParticipant.user.id.eq(userId))
        .fetch();
  }

  @Override
  public long countGuidebooksByParticipantId(UUID userId) {
    Long count = queryFactory.select(guidebook.count())
        .from(guidebookParticipant)
        .leftJoin(guidebookParticipant.guidebook, guidebook)
        .fetchJoin()
        .where(guidebookParticipant.user.id.eq(userId))
        .fetchOne();

    return count != null ? count : 0L;
  }
}
