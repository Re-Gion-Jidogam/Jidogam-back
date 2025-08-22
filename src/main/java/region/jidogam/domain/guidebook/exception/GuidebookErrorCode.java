package region.jidogam.domain.guidebook.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import region.jidogam.common.exception.ErrorCode;

@Getter
public enum GuidebookErrorCode implements ErrorCode {

  GUIDE_BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "GUIDE_BOOK_001", "존재하지 않는 가이드북입니다."),
  GUIDEBOOK_BACKGROUND_REQUIRED(HttpStatus.BAD_REQUEST, "GUIDE_BOOK_002",
    "가이드북은 배경(컬러 또는 썸네일) 중 하나는 필수입니다.");

  private HttpStatus status;
  private String code;
  private String message;

  GuidebookErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
