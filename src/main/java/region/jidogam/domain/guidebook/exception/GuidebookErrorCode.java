package region.jidogam.domain.guidebook.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import region.jidogam.common.exception.ErrorCode;

@Getter
public enum GuidebookErrorCode implements ErrorCode {

  GUIDE_BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "GUIDE_BOOK_001", "존재하지 않는 가이드북입니다.");

  private HttpStatus status;
  private String code;
  private String message;

  GuidebookErrorCode(HttpStatus status , String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
