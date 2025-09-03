package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookNotFoundException extends GuidebookException {

  public GuidebookNotFoundException(String message) {
    super(GuidebookErrorCode.GUIDE_BOOK_NOT_FOUND, message);
  }

  public static GuidebookNotFoundException withId(UUID id) {
    return new GuidebookNotFoundException(id + " 가이드북이 존재하지 않습니다.");
  }
}
