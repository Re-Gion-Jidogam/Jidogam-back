package region.jidogam.domain.place.exception;

import java.util.UUID;

public class PlaceNotFoundException extends PlaceException {

  public PlaceNotFoundException(String message) {
    super(PlaceErrorCode.PLACE_NOT_FOUND, message);
  }

  public static PlaceNotFoundException withId(UUID id) {
    return new PlaceNotFoundException(id + " 장소는 존재하지 않습니다.");
  }

  public static PlaceNotFoundException withKakaoId(String kakaoId) {
    return new PlaceNotFoundException("kakaoId가 " + kakaoId + "인 장소는 존재하지 않습니다.");
  }
}
