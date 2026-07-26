package region.jidogam.domain.area.exception;

public class InvalidAreaCodeException extends AreaException {

  public InvalidAreaCodeException(String message) {
    super(AreaErrorCode.INVALID_AREA_CODE, message);
  }

  public static InvalidAreaCodeException withSidoCode(String code) {
    return new InvalidAreaCodeException(
        String.format("시도 코드는 2자리여야 합니다. code=%s", code));
  }

  public static InvalidAreaCodeException withSigunguCode(String code) {
    return new InvalidAreaCodeException(
        String.format("시군구 코드는 5자리여야 합니다. code=%s", code));
  }
}
