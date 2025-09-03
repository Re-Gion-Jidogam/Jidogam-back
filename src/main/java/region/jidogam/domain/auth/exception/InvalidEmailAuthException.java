package region.jidogam.domain.auth.exception;

public class InvalidEmailAuthException extends EmailAuthException{

  public InvalidEmailAuthException(String message) {
    super(EmailAuthErrorCode.INVALID_AUTH_CODE, message);
  }

  public static InvalidEmailAuthException withCode(String code) {
    return new InvalidEmailAuthException("'"+ code + "'은(는) 유효하지 않은 이메일 인증 번호 입니다.");
  }
}