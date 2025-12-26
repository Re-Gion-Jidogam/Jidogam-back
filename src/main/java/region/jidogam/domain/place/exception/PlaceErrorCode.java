package region.jidogam.domain.place.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import region.jidogam.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements ErrorCode {

  PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_001", "존재하지 않는 장소입니다."),
  PLACE_NOT_MISMATCH(HttpStatus.BAD_REQUEST, "PLACE_002", "장소 정보가 유효하지 않습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
