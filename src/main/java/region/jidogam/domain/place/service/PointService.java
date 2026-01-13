package region.jidogam.domain.place.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PointService {

  @Value("${jidogam.points.base-point}")
  private int basePoint;

  /**
   * 지역 가중치 기반 장소 포인트 계산
   *
   * @param weight
   * @return 장소 포인트
   */
  public int calculatePlacePoint(Double weight) {
    return (int) (weight * basePoint);
  }
}
