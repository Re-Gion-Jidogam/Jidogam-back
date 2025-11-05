package region.jidogam.domain.auth.exception;

import region.jidogam.common.exception.ErrorCode;
import region.jidogam.common.exception.JidogamException;

public class PasswordResetException extends JidogamException {

  public PasswordResetException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

}
