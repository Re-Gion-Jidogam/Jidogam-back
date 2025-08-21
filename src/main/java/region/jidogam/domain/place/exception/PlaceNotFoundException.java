package region.jidogam.domain.place.exception;

public class PlaceNotFoundException extends PlaceException {

  public PlaceNotFoundException(String message) {
    super(PlaceErrorCode.PLACE_NOT_FOUND, message);
  }

  public static PlaceNotFoundException withPlaceName(String name) {
    return new PlaceNotFoundException("'" + name + "' 장소는 존재하지 않습니다.");
  }
}
