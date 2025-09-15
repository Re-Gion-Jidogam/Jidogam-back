package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookNotPublishedException extends GuidebookException {

  public GuidebookNotPublishedException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_NOT_PUBLISHED, message);
  }

  public static GuidebookNotPublishedException withId(UUID id) {
    return new GuidebookNotPublishedException(id + " 는 출판되지 않은 가이드북으로 참여가 불가능합니다.");
  }
}
