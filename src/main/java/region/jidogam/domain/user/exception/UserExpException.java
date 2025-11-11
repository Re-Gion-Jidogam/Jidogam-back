package region.jidogam.domain.user.exception;


import region.jidogam.common.exception.CommonErrorCode;

public class UserExpException extends UserException {

  public UserExpException(String message) {
    super(CommonErrorCode.INTERNAL_SERVER_ERROR, message);
  }

  public static UserExpException negativeValue() {
    return new UserExpException("경험치 변경 값은 음수가 될 수 없습니다.");
  }

}