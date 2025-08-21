package region.jidogam.domain.stamp.exception;

public class StampDuplicateException extends StampException {

  public StampDuplicateException(String message) {
    super(StampErrorCode.STAMP_ALREADY_EXISTS, message);
  }

  public static StampDuplicateException withPlaceName(String placeName) {
    return new StampDuplicateException("'" + placeName + "' 장소에 이미 도장이 있습니다.");
  }
}
