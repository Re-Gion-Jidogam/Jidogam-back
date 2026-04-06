package region.jidogam.domain.user.exception;

import region.jidogam.common.exception.ErrorCode;
import region.jidogam.common.exception.JidogamException;

public class UserException extends JidogamException {

  public UserException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

}
