package region.jidogam.domain.guidebook.repository.querydsl;

import java.util.List;
import java.util.UUID;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.domain.guidebook.dto.ParticipationFilter;
import region.jidogam.domain.guidebook.entity.GuidebookParticipation;
import region.jidogam.domain.user.dto.GuidebookParticipationCursor;

public interface GuidebookParticipationRepositoryCustom {

  List<GuidebookParticipation> searchParticipatingGuidebooks(
      UUID userId,
      GuidebookParticipationCursor cursor,
      String keyword,
      SortDirection sortDirection,
      ParticipationFilter filter,
      int limit
  );

  long countParticipatingGuidebooks(UUID userId, String keyword, ParticipationFilter filter);
}
