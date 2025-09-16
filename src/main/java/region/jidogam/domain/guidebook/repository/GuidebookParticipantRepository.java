package region.jidogam.domain.guidebook.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookParticipant;
import region.jidogam.domain.user.entity.User;

public interface GuidebookParticipantRepository extends JpaRepository<GuidebookParticipant, UUID> {

  boolean existsByGuidebookAndUser(Guidebook guidebook, User user);

  int deleteByGuidebook_IdAndUser_Id(UUID guidebookId, UUID userId);
}
