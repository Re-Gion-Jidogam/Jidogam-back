package region.jidogam.domain.user.exception;

public class UserDeletedException extends UserException {

  public UserDeletedException(String message) {
    super(UserErrorCode.USER_DELETED, message);
  }

  public static UserDeletedException withEmail(String email) {
    return new UserDeletedException("email = '" + email + "'인 사용자는 탈퇴한 상태입니다.");
  }
}
