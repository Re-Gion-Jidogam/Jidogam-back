package region.jidogam.domain.auth.exception;

public class InvalidPasswordResetTokenException extends PasswordResetException{

  public InvalidPasswordResetTokenException(String message) {
    super(PasswordResetErrorCode.INVALID_TOKEN, message);
  }

  public static InvalidPasswordResetTokenException withToken(String token) {
    return new InvalidPasswordResetTokenException("'"+ token.substring(0,5)+"*****"+ "'은(는) 유효하지 않은 토큰 입니다.");
  }
}