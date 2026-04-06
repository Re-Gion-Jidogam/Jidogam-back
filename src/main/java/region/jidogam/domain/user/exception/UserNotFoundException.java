package region.jidogam.domain.user.exception;

import java.util.UUID;

public class UserNotFoundException extends UserException {

  public UserNotFoundException(String message) {
    super(UserErrorCode.USER_NOT_FOUND, message);
  }

  public static UserNotFoundException withId(UUID id) {
    return new UserNotFoundException("id = '"+ id + "'인 사용자가 존재하지 않습니다.");
  }

  public static UserNotFoundException withEmail(String email) {
    return new UserNotFoundException("email = '"+ email + "'인 사용자가 존재하지 않습니다. ");
  }

}
