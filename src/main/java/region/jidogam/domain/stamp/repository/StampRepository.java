package region.jidogam.domain.stamp.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import region.jidogam.domain.stamp.entity.Stamp;

public interface StampRepository extends JpaRepository<Stamp, UUID> {

  Optional<Stamp> findFirstByUser_IdOrderByCreatedAtDesc(UUID uuid);

  boolean existsByUser_IdAndPlace_Id(UUID placeId, UUID userId);

  int deleteByUser_IdAndPlace_Id(UUID placeId, UUID userId);

  @Query("""
    SELECT COUNT(s)
    FROM Stamp s
    JOIN GuidebookPlace gp ON s.place.id = gp.place.id
    WHERE s.user.id = :userId AND gp.guidebook.id = :guidebookId
    """)
  int countUserStampsInGuidebook(
    @Param("userId") UUID userId,
    @Param("guidebookId") UUID guidebookId
  );
}
