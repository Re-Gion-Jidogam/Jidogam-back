package region.jidogam.domain.user.exception;

public class UserEmailConflictException extends UserException {

  public UserEmailConflictException(String message) {
    super(UserErrorCode.EMAIL_CONFLICT, message);
  }

  public static UserEmailConflictException withEmail(String email) {
    return new UserEmailConflictException("'"+ email + "'은/는 이미 존재하는 이메일입니다.");
  }

}
