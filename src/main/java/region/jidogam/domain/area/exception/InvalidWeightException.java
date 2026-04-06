package region.jidogam.domain.area.exception;

public class InvalidWeightException extends AreaException {

  public InvalidWeightException(String message) {
    super(AreaErrorCode.AREA_WEIGHT_IS_NULL, message);
  }

  public static InvalidWeightException setWeightNull(String addressName) {
    return new InvalidWeightException(String.format("'%s' 의 weight 값이 없습니다.", addressName));
  }
}
