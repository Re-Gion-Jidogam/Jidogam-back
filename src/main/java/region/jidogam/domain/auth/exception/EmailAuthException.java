package region.jidogam.domain.auth.exception;

import region.jidogam.common.exception.ErrorCode;
import region.jidogam.common.exception.JidogamException;

public class EmailAuthException extends JidogamException {

  public EmailAuthException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

}
