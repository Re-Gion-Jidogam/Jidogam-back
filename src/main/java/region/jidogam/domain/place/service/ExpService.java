package region.jidogam.domain.place.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExpService {

  @Value("${jidogam.exp.base-exp}")
  private int baseExp;

  /**
   * 지역 가중치 기반 장소 포인트 계산
   *
   * @param areaWeight
   * @return 장소 포인트
   */
  public int calculatePlaceExp(Double areaWeight) {
    return (int) (areaWeight * baseExp);
  }
}
