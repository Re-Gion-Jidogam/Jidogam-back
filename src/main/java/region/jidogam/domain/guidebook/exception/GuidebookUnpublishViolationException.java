package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookUnpublishViolationException extends GuidebookException {

  public GuidebookUnpublishViolationException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_UNPUBLISH_VIOLATION, message);
  }

  public static GuidebookUnpublishViolationException withId(UUID id) {
    return new GuidebookUnpublishViolationException(id + " 가이드북은 구독자가 존재하여 출판을 취소할 수 없습니다.");
  }
}
