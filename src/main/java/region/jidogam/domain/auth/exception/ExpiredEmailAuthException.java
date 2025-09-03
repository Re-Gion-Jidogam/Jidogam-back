package region.jidogam.domain.auth.exception;

public class ExpiredEmailAuthException extends EmailAuthException{

  public ExpiredEmailAuthException(String message) {
    super(EmailAuthErrorCode.EXPIRED_AUTH_CODE, message);
  }

  public static ExpiredEmailAuthException withCode(String code) {
    return new ExpiredEmailAuthException("'"+ code + "'은(는) 이미 만료된 인증번호 입니다.");
  }

}
