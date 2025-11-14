package region.jidogam.domain.user.exception;

public class UnauthorizedUserException extends UserException {

  public UnauthorizedUserException(String message) {
    super(UserErrorCode.NO_PERMISSION, message);
  }

  public static UnauthorizedUserException noPermission() {
    return new UnauthorizedUserException("접근 권한이 없습니다.");
  }

}
