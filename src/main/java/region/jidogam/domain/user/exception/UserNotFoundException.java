package region.jidogam.domain.user.exception;

public class UserNotFoundException extends UserException {

  public UserNotFoundException(String message) {
    super(UserErrorCode.USER_NOT_FOUND, message);
  }

  public static UserNotFoundException withId(String id) {
    return new UserNotFoundException(id + " not found");
  }

}
