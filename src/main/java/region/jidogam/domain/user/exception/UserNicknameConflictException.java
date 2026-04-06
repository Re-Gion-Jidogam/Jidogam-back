package region.jidogam.domain.user.exception;

public class UserNicknameConflictException extends UserException {

  public UserNicknameConflictException(String message) {
    super(UserErrorCode.NICKNAME_CONFLICT, message);
  }

  public static UserNicknameConflictException withNickname(String nickname) {
    return new UserNicknameConflictException("'"+ nickname + "'은/는 이미 존재하는 닉네임입니다.");
  }

}
