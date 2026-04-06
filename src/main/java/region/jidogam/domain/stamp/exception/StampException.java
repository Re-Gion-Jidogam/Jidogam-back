package region.jidogam.domain.stamp.exception;

import region.jidogam.common.exception.ErrorCode;
import region.jidogam.common.exception.JidogamException;

public class StampException extends JidogamException {

  public StampException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
