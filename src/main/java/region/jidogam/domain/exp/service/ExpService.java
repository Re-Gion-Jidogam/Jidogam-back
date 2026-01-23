package region.jidogam.domain.exp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExpService {

  @Value("${jidogam.exp.base-exp}")
  private int baseExp;

  @Value("${jidogam.exp.guidebook-completion-rate}")
  private double guidebookCompletionRate;

  /**
   * 지역 가중치 기반 장소 포인트 계산
   *
   * @param areaWeight 지역 가중치
   * @return 장소 포인트
   */
  public int calculatePlaceExp(Double areaWeight) {
    return (int) (areaWeight * baseExp);
  }

  /**
   * 가이드북 완료 보상 경험치 계산
   *
   * @param totalExp 가이드북 참여 일자 기준으로 이후 획득한 총 경험치
   * @return 완료 보상 경험치
   */
  public int calculateGuidebookCompletionExp(int totalExp) {
    return (int) (totalExp * guidebookCompletionRate);
  }
}
