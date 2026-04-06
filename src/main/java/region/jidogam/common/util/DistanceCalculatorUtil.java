package region.jidogam.common.util;

import org.springframework.stereotype.Component;
import region.jidogam.common.dto.CoordinateRange;

@Component
public class DistanceCalculatorUtil {

  private static final int EARTH_RADIUS_KM = 6371;

  /**
   * Haversine 공식으로 두 지점 간 거리 계산
   *
   * @param userLat  사용자의 위도 (degrees, -90 ~ 90)
   * @param userLon  사용자의 경도 (degrees, -180 ~ 180)
   * @param placeLat 장소의 위도 (degrees, -90 ~ 90)
   * @param placeLon 장소의 경도 (degrees, -180 ~ 180)
   * @return 거리(km), 사용자 위치 없으면 null
   */
  public static Double calculateDistance(Double userLat, Double userLon, double placeLat,
      double placeLon) {
    if (userLat == null || userLon == null) {
      return null;
    }

    double dLat = Math.toRadians(placeLat - userLat);
    double dLon = Math.toRadians(placeLon - userLon);

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
        + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(placeLat))
        * Math.sin(dLon / 2) * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = EARTH_RADIUS_KM * c;

    return Math.round(distance * 1000.0) / 1000.0;
  }

  /**
   * 특정 좌표로부터 일정 거리 내의 위도/경도 범위를 계산
   * 위도 1도당 거리 (약 111km), 경도 1도당 거리는 위도에 따라 변함
   *
   * @param latitude   중심점의 위도
   * @param longitude  중심점의 경도
   * @param distanceKm 반경 거리 (km)
   * @return 계산된 좌표 범위 (latMin, latMax, lonMin, lonMax)
   */
  public static CoordinateRange getCoordinateRange(Double latitude, Double longitude,
      Double distanceKm) {
    if (latitude == null || longitude == null || distanceKm == null) {
      throw new IllegalArgumentException("위도, 경도, 거리 값은 필수입니다.");
    }

    double latDegree = distanceKm / 111.0;
    double lonDegree = distanceKm / (111.0 * Math.cos(Math.toRadians(latitude)));

    return CoordinateRange.builder()
        .latMin(latitude - latDegree)
        .latMax(latitude + latDegree)
        .lonMin(longitude - lonDegree)
        .lonMax(longitude + lonDegree)
        .build();
  }
}
