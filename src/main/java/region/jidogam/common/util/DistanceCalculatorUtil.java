package region.jidogam.common.util;

import org.springframework.stereotype.Component;

@Component
public class DistanceCalculatorUtil {

  private static final int EARTH_RADIUS_KM = 6371;

  /**
   * Haversine 공식으로 두 지점 간 거리 계산
   *
   * @return 거리(km), 사용자 위치 없으면 null
   */
  public static Double calculate(Double userLat, Double userLon, double placeLat, double placeLon) {
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

    return Math.round(distance * 100.0) / 100.0; // 소수점 2자리
  }
}
