package region.jidogam.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode implements ErrorCode {

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_001", "서버에서 알 수 없는 문제가 발생했습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COMMON_002", "지원하지 않는 Request Method 입니다."),
  INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "COMMON_003", "지원하지 않는 JSON 형식입니다."),
  URI_NOT_FOUND(HttpStatus.BAD_REQUEST, "COMMON_004", "요청 URI를 찾을 수 없습니다."),
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_005", "잘못된 입력입니다."),
  INVALID_CURSOR(HttpStatus.BAD_REQUEST, "COMMON_006", "잘못된 커서 값 입니다.");

  private HttpStatus status;
  private String code;
  private final String message;

  CommonErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
