package region.jidogam.common.exception;

public class InvalidCursorException extends JidogamException {

  public InvalidCursorException(String message) {
    super(CommonErrorCode.INVALID_CURSOR);
  }

  public static InvalidCursorException withMessage(String message) {
    return new InvalidCursorException(message);
  }
}
