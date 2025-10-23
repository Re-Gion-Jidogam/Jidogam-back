package region.jidogam.domain.guidebook.repository.querydsl;

import java.util.List;
import java.util.UUID;
import region.jidogam.domain.guidebook.dto.GuidebookWithAuthor;

public interface GuidebookParticipantRepositoryCustom {

  List<GuidebookWithAuthor> searchGuidebooksByParticipantId(UUID userId);

  long countGuidebooksByParticipantId(UUID guidebookId);
}
