package region.jidogam.domain.stamp.exception;

import java.util.UUID;

public class StampNotFoundException extends StampException {

  public StampNotFoundException(String message) {
    super(StampErrorCode.STAMP_NOT_FOUND, message);
  }

  public static StampNotFoundException withPlaceIdAndUserId(UUID userId, UUID placeId) {
    return new StampNotFoundException(userId + "님의 " + placeId + "에 대한 도장이 존재하지 않습니다.");
  }
}
