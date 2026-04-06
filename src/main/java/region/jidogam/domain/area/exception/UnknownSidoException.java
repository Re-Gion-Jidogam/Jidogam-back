package region.jidogam.domain.area.exception;

public class UnknownSidoException extends AreaException {

  public UnknownSidoException(String message) {
    super(AreaErrorCode.UNKNOWN_SIDO, message);
  }

  public static UnknownSidoException withSido(String sido) {
    return new UnknownSidoException("'" + sido + "'와 매칭되는 시도가 없습니다.");
  }
}
