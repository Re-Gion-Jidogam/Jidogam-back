package region.jidogam.domain.place.exception;

public class PlaceDuplicateException extends PlaceException {

  public PlaceDuplicateException(String message) {
    super(PlaceErrorCode.PLACE_DUPLICATE, message);
  }

  public static PlaceDuplicateException withKakaoId(String kakaoId) {
    return new PlaceDuplicateException("이미 등록된 장소입니다: kakaoId = " + kakaoId);
  }
}
