package region.jidogam.domain.user.exception;

public class UserNicknameConflictException extends UserException {

  public UserNicknameConflictException(String message) {
    super(UserErrorCode.NICKNAME_CONFLICT, message);
  }

  public static UserNicknameConflictException withNickname(String nickname) {
    return new UserNicknameConflictException(nickname + " already exists");
  }

}
