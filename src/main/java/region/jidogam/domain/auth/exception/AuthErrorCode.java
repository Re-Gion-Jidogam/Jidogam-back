package region.jidogam.domain.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import region.jidogam.common.exception.ErrorCode;

@Getter
public enum AuthErrorCode implements ErrorCode {

  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "잘못된 인증 정보입니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_002", "권한이 없습니다.");

  private HttpStatus status;
  private String code;
  private final String message;

  AuthErrorCode(HttpStatus status , String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
