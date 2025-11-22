package region.jidogam.domain.user.exception;

public class UserRestorePeriodExpiredException extends UserException {

  public UserRestorePeriodExpiredException(String message) {
    super(UserErrorCode.USER_RESTORE_PERIOD_EXPIRED, message);
  }

  public static UserRestorePeriodExpiredException withEmail(String email) {
    return new UserRestorePeriodExpiredException(
        "email = '" + email + "'인 사용자의 복구 가능 기간이 만료되었습니다.");
  }
}
