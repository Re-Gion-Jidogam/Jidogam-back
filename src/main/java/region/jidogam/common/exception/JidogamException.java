package region.jidogam.common.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class JidogamException extends RuntimeException {
    private final Map<String, Object> details = new HashMap<>();
    private final CommonErrorCode commonErrorCode;

    public JidogamException(CommonErrorCode commonErrorCode) {
      super(commonErrorCode.getMessage(), null, false, false);
      this.commonErrorCode = commonErrorCode;
    }

    public JidogamException(CommonErrorCode commonErrorCode, Throwable cause) {
      super(commonErrorCode.getMessage(), cause);
      this.commonErrorCode = commonErrorCode;
    }

    public JidogamException(CommonErrorCode commonErrorCode, Map<String, Object> details){
      this.commonErrorCode = commonErrorCode;
      this.details.putAll(details);
    }

    public JidogamException(CommonErrorCode commonErrorCode, String message) {
      super(message);
      this.commonErrorCode = commonErrorCode;
    }
  }
