package region.jidogam.domain.guidebook.exception;

import region.jidogam.common.exception.ErrorCode;
import region.jidogam.common.exception.JidogamException;

public class GuidebookException extends JidogamException {

  public GuidebookException(ErrorCode errorCode) {
    super(errorCode);
  }

  public GuidebookException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
