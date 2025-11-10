package region.jidogam.domain.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import region.jidogam.common.exception.ErrorCode;

@Getter
public enum PasswordResetErrorCode implements ErrorCode {

  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "PW_001", "토큰이 유효하지 않습니다."),
  ALREADY_USED_TOKEN(HttpStatus.UNAUTHORIZED, "PW_003", "이미 사용된 토큰 입니다.");

  private HttpStatus status;
  private String code;
  private final String message;

  PasswordResetErrorCode(HttpStatus status , String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
