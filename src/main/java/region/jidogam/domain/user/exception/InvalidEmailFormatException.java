package region.jidogam.domain.user.exception;

public class InvalidEmailFormatException extends UserException{

  public InvalidEmailFormatException(String message) {
    super(UserErrorCode.EMAIL_FORMAT_INVALID, message);
  }

  public static InvalidEmailFormatException withEmail(String email) {
    return new InvalidEmailFormatException("'" + email +"' 은 유효하지 않은 이메일 형식입니다.");
  }
}
