package region.jidogam.domain.user.exception;

public class UnverifiedEmailException extends UserException {
  public UnverifiedEmailException(String message) {
    super(UserErrorCode.EMAIL_NOT_VARIFIED, message);
  }

  public static UnverifiedEmailException withEmail(String email) {
    return new UnverifiedEmailException("'" + email +"' 은 인증되지 않은 이메일입니다.");
  }
}
