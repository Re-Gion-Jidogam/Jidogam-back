package region.jidogam.domain.stamp.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import region.jidogam.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum StampErrorCode implements ErrorCode {

  STAMP_NOT_FOUND(HttpStatus.NOT_FOUND, "STAMP_001", "이미 도장을 찍은 장소입니다."),
  STAMP_ALREADY_EXISTS(HttpStatus.CONFLICT, "STAMP_002", "이미 도장을 찍은 장소입니다."),
  STAMP_COOLDOWN_ACTIVE(HttpStatus.BAD_REQUEST, "STAMP_003", "도장은 연속으로 찍을 수 없습니다.");


  private final HttpStatus status;
  private final String code;
  private final String message;
}
