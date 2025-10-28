package region.jidogam.domain.guidebook.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import region.jidogam.domain.guidebook.dto.AreaRatioDto;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookPlace;

public interface GuidebookPlaceRepository extends JpaRepository<GuidebookPlace, UUID> {

  int deleteByGuidebook_IdAndPlace_Id(UUID guidebookId, UUID placeId);

  void deleteByGuidebook(Guidebook guidebook);

  @Query("""
      SELECT new region.jidogam.domain.guidebook.dto.AreaRatioDto (
            a,
            COUNT(gp.id)
      )
      FROM GuidebookPlace gp
      JOIN gp.place p
      JOIN p.area a
      WHERE gp.guidebook.id = :guidebookId
      GROUP BY a.id
      ORDER BY COUNT(gp.id) DESC
      """)
  List<AreaRatioDto> findAreasByPlaceCountDesc(
      @Param("guidebookId") UUID guidebookId,
      Pageable pageable
  );
}
