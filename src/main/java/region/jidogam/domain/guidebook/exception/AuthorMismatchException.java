package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class AuthorMismatchException extends GuidebookException {

  public AuthorMismatchException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_AUTHOR_MISMATCH, message);
  }

  public static AuthorMismatchException withId(UUID id) {
    return new AuthorMismatchException(id + " 가이드북 작성자가 아닙니다.");
  }
}
