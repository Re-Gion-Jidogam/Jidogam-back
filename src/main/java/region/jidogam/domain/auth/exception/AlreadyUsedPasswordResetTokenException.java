package region.jidogam.domain.auth.exception;

public class AlreadyUsedPasswordResetTokenException extends PasswordResetException{

  public AlreadyUsedPasswordResetTokenException(String message) {
    super(PasswordResetErrorCode.ALREADY_USED_TOKEN, message);
  }

  public static AlreadyUsedPasswordResetTokenException withToken(String token) {
    return new AlreadyUsedPasswordResetTokenException("'"+ token.substring(0,5)+"*****"+ "'은(는) 이미 사용된 토큰 입니다.");
  }
}