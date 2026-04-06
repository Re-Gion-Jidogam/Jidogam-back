package region.jidogam.domain.user.exception;

public class UserNicknameLengthException extends UserException {

  public UserNicknameLengthException(String message) {
    super(UserErrorCode.NICKNAME_LENGTH_INVALID, message);
  }

  public static UserNicknameLengthException withNickname(String nickname) {
    return new UserNicknameLengthException("'"+nickname + "'은/는 유효하지 않은 길이의 닉네임입니다.");
  }
}
