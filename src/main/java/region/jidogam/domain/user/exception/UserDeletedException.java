package region.jidogam.domain.user.exception;

public class UserDeletedException extends UserException {

  public UserDeletedException(String message) {
    super(UserErrorCode.USER_DELETED, message);
  }

  public static UserDeletedException withEmail(String email) {
    String maskedEmail = email.substring(0, Math.min(2, email.indexOf('@')))
        + "****" + email.substring(email.indexOf('@'));
    return new UserDeletedException("email = '" + maskedEmail + "'인 사용자는 탈퇴한 상태입니다.");
  }
}
