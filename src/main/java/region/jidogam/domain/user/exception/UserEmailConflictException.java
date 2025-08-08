package region.jidogam.domain.user.exception;

public class UserEmailConflictException extends UserException {

  public UserEmailConflictException(String message) {
    super(UserErrorCode.EMAIL_CONFLICT, message);
  }

  public static UserEmailConflictException withEmail(String email) {
    return new UserEmailConflictException(email + " already exists");
  }

}
