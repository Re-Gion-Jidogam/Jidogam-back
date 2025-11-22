package region.jidogam.domain.user.exception;

import java.util.UUID;

public class UserAlreadyDeletedException extends UserException {

  public UserAlreadyDeletedException(String message) {
    super(UserErrorCode.USER_ALREADY_DELETED, message);
  }

  public static UserAlreadyDeletedException withId(UUID id) {
    return new UserAlreadyDeletedException("id = '" + id + "'인 사용자는 이미 탈퇴한 상태입니다.");
  }
}
