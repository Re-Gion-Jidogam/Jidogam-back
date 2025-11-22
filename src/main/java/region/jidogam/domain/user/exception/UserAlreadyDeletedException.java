package region.jidogam.domain.user.exception;

public class UserAlreadyDeletedException extends UserException {

  public UserAlreadyDeletedException(String message) {
    super(UserErrorCode.USER_ALREADY_DELETED, message);
  }

  public static UserAlreadyDeletedException withEmail(String email) {
    String maskedEmail = email.substring(0, Math.min(2, email.indexOf('@')))
        + "****" + email.substring(email.indexOf('@'));
    return new UserAlreadyDeletedException("email = '" + maskedEmail + "'인 사용자는 이미 탈퇴한 상태입니다.");
  }
}
