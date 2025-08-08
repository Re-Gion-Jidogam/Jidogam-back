package region.jidogam.common.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class JidogamException extends RuntimeException {
    private final Map<String, Object> details = new HashMap<>();
    private final ErrorCode errorCode;

    public JidogamException(ErrorCode errorCode) {
      super(errorCode.getMessage(), null, false, false);
      this.errorCode = errorCode;
    }

    public JidogamException(ErrorCode errorCode, Throwable cause) {
      super(errorCode.getMessage(), cause);
      this.errorCode = errorCode;
    }

    public JidogamException(ErrorCode errorCode, Map<String, Object> details){
      this.errorCode = errorCode;
      this.details.putAll(details);
    }

    public JidogamException(ErrorCode errorCode, String message) {
      super(message);
      this.errorCode = errorCode;
    }

    public void addDetail(String key, Object value){
      details.put(key, value);
    }
  }
