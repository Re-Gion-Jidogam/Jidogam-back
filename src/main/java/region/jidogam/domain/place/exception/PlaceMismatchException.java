package region.jidogam.domain.place.exception;

import java.util.UUID;

public class PlaceMismatchException extends PlaceException {

  public PlaceMismatchException(String message) {
    super(PlaceErrorCode.PLACE_NOT_MISMATCH, message);
  }

  public static PlaceMismatchException idMismatch(UUID placeId, String kakaoId) {
    return new PlaceMismatchException(
        String.format("Place ID(%s)와 Kakao ID(%s)가 일치하지 않습니다.", placeId, kakaoId)
    );
  }
}
