package region.jidogam.domain.area.exception;

public class UnknownSidoException extends AreaException {

  public UnknownSidoException(String message) {
    super(AreaErrorCode.UNKNOWN_SIDO, message);
  }

  public static UnknownSidoException withSido(String sido) {
    return new UnknownSidoException("Unknown sido alias: " + sido);
  }
}
