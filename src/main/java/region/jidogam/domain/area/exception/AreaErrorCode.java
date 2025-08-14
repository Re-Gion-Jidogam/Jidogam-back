package region.jidogam.domain.area.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import region.jidogam.common.exception.ErrorCode;

@Getter
public enum AreaErrorCode implements ErrorCode {
  UNKNOWN_SIDO(HttpStatus.BAD_REQUEST, "AREA_001", "잘못된 시도 값입니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

  AreaErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}


