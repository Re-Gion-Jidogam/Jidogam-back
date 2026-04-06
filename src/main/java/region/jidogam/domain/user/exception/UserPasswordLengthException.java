package region.jidogam.domain.user.exception;

public class UserPasswordLengthException extends UserException {

  public UserPasswordLengthException(String message) {
    super(UserErrorCode.PASSWORD_LENGTH_INVALID, message);
  }

  public static UserPasswordLengthException lengthInvalid() {
    return new UserPasswordLengthException("비밀번호는 8자 이상이어야 합니다.");
  }
}
