package region.jidogam.domain.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import region.jidogam.common.exception.ErrorCode;

@Getter
public enum UserErrorCode implements ErrorCode {

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
  EMAIL_CONFLICT(HttpStatus.CONFLICT, "USER_OO2", "이미 존재하는 이메일입니다."),
  NICKNAME_CONFLICT(HttpStatus.CONFLICT, "USER_OO3", "이미 존재하는 닉네임입니다."),
  NICKNAME_LENGTH_INVALID(HttpStatus.BAD_REQUEST, "USER_O4", "닉네임은 2자 이상, 20자 이하여야 합니다."),
  EMAIL_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "USER_05", "유효하지 않은 이메일 형식입니다."),
  EMAIL_NOT_VARIFIED(HttpStatus.FORBIDDEN, "USER_06", "인증된 이메일로만 회원가입할 수 있습니다."),
  PASSWORD_LENGTH_INVALID(HttpStatus.BAD_REQUEST, "USER_O7", "비밀번호는 8자 이상이어야 합니다."),
  NO_PERMISSION(HttpStatus.FORBIDDEN, "USER_08", "접근 권한이 없습니다.");

  private HttpStatus status;
  private String code;
  private final String message;

  UserErrorCode(HttpStatus status , String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
