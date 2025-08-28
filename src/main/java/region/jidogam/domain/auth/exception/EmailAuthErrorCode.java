package region.jidogam.domain.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import region.jidogam.common.exception.ErrorCode;

@Getter
public enum EmailAuthErrorCode implements ErrorCode {

  INVALID_AUTH_CODE(HttpStatus.UNAUTHORIZED, "EMAILAUTH_001", "인증 코드가 일치하지 않습니다."),
  EMAIL_AUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAILAUTH_002", "해당 이메일로 인증 번호가 발송된 이력이 없습니다." ),
  EXPIRED_AUTH_CODE(HttpStatus.UNAUTHORIZED, "EMAILAUTH_003", "인증 시간이 만료된 인증 코드 입니다."),
  ALREADY_USED_AUTH_CODE(HttpStatus.UNAUTHORIZED, "EMAILAUTH_004", "이미 사용된 인증 코드 입니다.");

  private HttpStatus status;
  private String code;
  private final String message;

  EmailAuthErrorCode(HttpStatus status , String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
