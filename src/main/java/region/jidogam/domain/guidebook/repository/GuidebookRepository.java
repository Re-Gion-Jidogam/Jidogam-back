package region.jidogam.domain.guidebook.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.repository.querydsl.GuidebookRepositoryCustom;

public interface GuidebookRepository extends JpaRepository<Guidebook, UUID>,
  GuidebookRepositoryCustom {

  @Modifying
  @Query("""
    UPDATE Guidebook g
    SET g.participantCount = g.participantCount + :delta
    WHERE g.id = :guidebookId
    AND g.participantCount + :delta >= 0
    """)
  void updateParticipantCount(UUID guidebookId, int delta);
}
