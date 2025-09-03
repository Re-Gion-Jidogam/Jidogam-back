package region.jidogam.domain.auth.exception;


public class EmailAuthNotFoundException extends EmailAuthException{

  public EmailAuthNotFoundException(String message) {
    super(EmailAuthErrorCode.EMAIL_AUTH_NOT_FOUND, message);
  }

  public static EmailAuthNotFoundException withEmail(String email) {
    return new EmailAuthNotFoundException("'"+ email + "'로 발급한 인증 코드가 이미 만료되었거나 존재하지 않습니다.");
  }
}
