package region.jidogam.domain.user.exception;

import java.util.UUID;

public class UserNotFoundException extends UserException {

  public UserNotFoundException(String message) {
    super(UserErrorCode.USER_NOT_FOUND, message);
  }

  public static UserNotFoundException withId(UUID id) {
    return new UserNotFoundException(id + " not found");
  }

}
