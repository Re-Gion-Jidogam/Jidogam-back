package region.jidogam.domain.place.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import region.jidogam.domain.place.dto.PlaceVisitInfo;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.repository.querydsl.PlaceRepositoryCustom;

public interface PlaceRepository extends JpaRepository<Place, UUID>, PlaceRepositoryCustom {

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

  @Query(value = """
      WITH filtered_places AS (
        SELECT p.*
        FROM guidebook_places gp
        JOIN places p ON gp.place_id = p.id
        LEFT JOIN stamps s ON s.place_id = p.id AND s.user_id = :userId
        WHERE gp.guidebook_id = :guidebookId
          AND (
            :filter = 'none'
            OR (:filter = 'visited' AND s.id IS NOT NULL)
            OR (:filter = 'notVisited' AND s.id IS NULL)
          )
      ),
      with_distance AS (
        SELECT *,
          ROUND(
            CAST(
              (6371 * acos(
                cos(radians(:userLat)) * cos(radians(y))
                * cos(radians(x) - radians(:userLon))
                + sin(radians(:userLat)) * sin(radians(y))
              )) AS numeric
            ),
            3
          ) AS distance
        FROM filtered_places
      )
      SELECT *
      FROM with_distance
      WHERE (:lastDistance IS NULL AND :lastPlaceId IS NULL)
        OR (distance > :lastDistance)
        OR (distance = :lastDistance AND id > :lastPlaceId)
      ORDER BY distance, id
      LIMIT :size
      """, nativeQuery = true)
  List<Place> findPlacesByGuidebookOrderByDistance(
      @Param("userLat") Double userLat,
      @Param("userLon") Double userLon,
      @Param("userId") UUID userId,
      @Param("guidebookId") UUID guidebookId,
      @Param("filter") String filter,
      @Param("lastDistance") Double lastDistance,
      @Param("lastPlaceId") UUID lastPlaceId,
      @Param("size") int size
  );
  // gp의 guidebookId 인덱스 추가하기

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

  @Query("""
      SELECT new region.jidogam.domain.place.dto.PlaceVisitInfo(s.place.id, s.createdAt)
      FROM Stamp s
      WHERE s.user.id = :userId
        AND s.place.id IN :placeIds
      """)
  List<PlaceVisitInfo> findVisitedDatesByUserAndPlaces(
      @Param("userId") UUID userId,
      @Param("placeIds") List<UUID> placeIds
  );

}
