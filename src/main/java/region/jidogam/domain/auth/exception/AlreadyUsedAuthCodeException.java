package region.jidogam.domain.auth.exception;

public class AlreadyUsedAuthCodeException extends EmailAuthException{

  public AlreadyUsedAuthCodeException(String message) {
    super(EmailAuthErrorCode.ALREADY_USED_AUTH_CODE, message);
  }

  public static AlreadyUsedAuthCodeException withCode(String code) {
    return new AlreadyUsedAuthCodeException("'"+ code + "'은(는) 이미 사용된 인증 번호 입니다.");
  }
}
