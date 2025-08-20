package region.jidogam.domain.area.exception;

public class AreaNotFoundException extends AreaException {

  public AreaNotFoundException(String message) {
    super(AreaErrorCode.AREA_NOT_FOUND, message);
  }

  public static AreaNotFoundException withSidoAndSigungu(String sido, String sigungu) {
    return new AreaNotFoundException(sido + ", " + sigungu + " 정보가 존재하지 않습니다.");
  }
}
