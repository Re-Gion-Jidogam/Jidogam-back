package region.jidogam.domain.area.exception;

import region.jidogam.common.exception.ErrorCode;
import region.jidogam.common.exception.JidogamException;

public class AreaException extends JidogamException {

  public AreaException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
