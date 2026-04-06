package region.jidogam.domain.stamp.exception;

public class StampCooldownException extends StampException {

  public StampCooldownException(String message) {
    super(StampErrorCode.STAMP_COOLDOWN_ACTIVE, message);
  }

  public static StampCooldownException withRestTime(Long minute) {
    return new StampCooldownException("아직 도장을 찍을 수 없습니다. 최소 " + minute + "분 간격이 필요합니다.");
  }
}
