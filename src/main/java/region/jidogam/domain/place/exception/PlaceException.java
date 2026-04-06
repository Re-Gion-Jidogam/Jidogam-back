package region.jidogam.domain.place.exception;

import region.jidogam.common.exception.ErrorCode;
import region.jidogam.common.exception.JidogamException;

public class PlaceException extends JidogamException {

  public PlaceException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
