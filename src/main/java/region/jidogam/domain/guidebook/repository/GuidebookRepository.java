package region.jidogam.domain.guidebook.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import region.jidogam.domain.guidebook.entity.Guidebook;

public interface GuidebookRepository extends JpaRepository<Guidebook, UUID> {

  @Modifying
  @Query("""
    UPDATE Guidebook g
    SET g.participantCount = g.participantCount + :delta
    WHERE g.id = :guidebookId
    AND g.participantCount + :delta >= 0
    """)
  void updateParticipantCount(UUID guidebookId, int delta);

  List<Guidebook> findByAuthor_IdAndIsPublished(UUID authorId, Boolean isPublished);

  List<Guidebook> findByAuthor_Id(UUID authorId);

  long countByAuthor_IdAndIsPublished(UUID authorId, Boolean isPublished);

  long countByAuthor_Id(UUID authorId);
}
