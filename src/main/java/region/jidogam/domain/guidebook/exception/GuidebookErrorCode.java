package region.jidogam.domain.guidebook.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import region.jidogam.common.exception.ErrorCode;

@Getter
public enum GuidebookErrorCode implements ErrorCode {

  GUIDEBOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "GUIDE_BOOK_001", "존재하지 않는 가이드북입니다."),
  GUIDEBOOK_BACKGROUND_REQUIRED(HttpStatus.BAD_REQUEST, "GUIDE_BOOK_002",
    "가이드북 배경(컬러 또는 썸네일)은 필수 입니다."),
  GUIDEBOOK_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN, "GUIDE_BOOK_003",
    "가이드북 작성자가 아닙니다.");

  private HttpStatus status;
  private String code;
  private String message;

  GuidebookErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
