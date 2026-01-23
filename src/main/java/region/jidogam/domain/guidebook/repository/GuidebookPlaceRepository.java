package region.jidogam.domain.guidebook.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import region.jidogam.domain.guidebook.dto.AreaRatioDto;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookPlace;
import region.jidogam.domain.place.entity.Place;

public interface GuidebookPlaceRepository extends JpaRepository<GuidebookPlace, UUID> {

  int deleteByGuidebook_IdAndPlace_Id(UUID guidebookId, UUID placeId);

  void deleteByGuidebook(Guidebook guidebook);

  boolean existsByGuidebookAndPlace(Guidebook guidebook, Place place);

  /**
   * 가이드북에 포함된 장소들의 지역별 분포를 조회
   * 장소 개수가 많은 순으로 정렬하여 반환
   *
   * @param guidebookId 가이드북 ID
   * @param pageable    페이징 정보 (주로 상위 N개 지역만 조회하기 위해 사용)
   * @return 지역별 장소 개수 DTO 리스트 (장소 개수 내림차순)
   */
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

  /**
   * 특정 가이드북 목록 중 해당 장소를 포함하는 가이드북 ID 조회
   *
   * @param placeId      장소 ID
   * @param guidebookIds 가이드북 ID 리스트
   * @return 해당 장소를 포함하는 가이드북 ID 집합
   */
  @Query("""
      SELECT gp.guidebook.id
      FROM GuidebookPlace gp
      WHERE gp.place.id = :placeId
      AND gp.guidebook.id IN :guidebookIds
      """)
  Set<UUID> findGuidebookIdsByPlaceIdAndGuidebookIds(
      @Param("placeId") UUID placeId,
      @Param("guidebookIds") List<UUID> guidebookIds
  );
}
