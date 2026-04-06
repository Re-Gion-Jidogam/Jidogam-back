package region.jidogam.domain.guidebook.exception;

public class ParticipationEarnedExpException extends GuidebookException {

  public ParticipationEarnedExpException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_PARTICIPATE_EARNED_EXP, message);
  }

  public static ParticipationEarnedExpException insufficientExp(int current, int required) {
    return new ParticipationEarnedExpException(
        String.format("차감할 가이드북 참여 경험치가 부족합니다. 현재: %d, 차감 시도: %d", current, required));
  }
}
