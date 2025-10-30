package region.jidogam.domain.place.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import region.jidogam.domain.place.entity.Place;

public interface PlaceRepository extends JpaRepository<Place, UUID> {

  List<Place> findAllByOrderByStampCountDesc(Pageable pageable);

  /**
   * 특정 좌표로부터 일정 거리 내의 장소를 거리순으로 조회
   *
   * @param userLat     사용자 위도
   * @param userLon     사용자 경도
   * @param latMin      위도 최소값
   * @param latMax      위도 최대값
   * @param lonMin      경도 최소값
   * @param lonMax      경도 최대값
   * @param maxDistance 최대 거리 (km)
   * @param pageable    조회할 최대 개수
   * @return 거리순으로 정렬된 장소 목록
   */
  @Query(value = """
      SELECT * FROM (
        SELECT p.*,
          (6371 * acos(
            cos(radians(:userLat)) * cos(radians(p.y)) 
            * cos(radians(p.x) - radians(:userLon)) 
            + sin(radians(:userLat)) * sin(radians(p.y))
          )) AS distance
        FROM places p
        WHERE p.y BETWEEN :latMin AND :latMax
          AND p.x BETWEEN :lonMin AND :lonMax
      ) AS places_with_distance
      WHERE distance <= :maxDistance
      ORDER BY distance
      """, nativeQuery = true)
  List<Place> findNearbyPlaces(
      @Param("userLat") Double userLat,
      @Param("userLon") Double userLon,
      @Param("latMin") Double latMin,
      @Param("latMax") Double latMax,
      @Param("lonMin") Double lonMin,
      @Param("lonMax") Double lonMax,
      @Param("maxDistance") Double maxDistance,
      Pageable pageable
  );

  @Modifying
  @Query("""
      UPDATE Place p
      SET p.stampCount = p.stampCount + :delta
      WHERE p.id = :placeId
      AND p.stampCount + :delta >= 0
      """)
  void updateStampCount(UUID placeId, int delta);

  @Modifying
  @Query("""
      UPDATE Place p
      SET p.guidebookCount = p.guidebookCount + :delta
      WHERE p.id = :placeId
      AND p.guidebookCount + :delta >= 0
      """)
  void updateGuidebookCount(UUID placeId, int delta);
}
