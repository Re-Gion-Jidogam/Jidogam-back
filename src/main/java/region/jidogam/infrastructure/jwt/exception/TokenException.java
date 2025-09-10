package region.jidogam.infrastructure.jwt.exception;

import region.jidogam.common.exception.JidogamException;

public class TokenException extends JidogamException {

  private TokenException(String message) {
    super(TokenErrorCode.INVALID_TOKEN, message);
  }

  public static TokenException withMessage(String message) {
    return new TokenException(message);
  }
}
