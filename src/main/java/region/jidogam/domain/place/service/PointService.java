package region.jidogam.domain.place.service;

import org.springframework.stereotype.Service;

@Service
public class PointService {

  /**
   * 지역 가중치 기반 장소 포인트 계산
   *
   * @param weight
   * @return 장소 포인트
   */
  public int calculatePlacePoint(Integer weight) {
    return weight * 10; // 임시
  }
}
