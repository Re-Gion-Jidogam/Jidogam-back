package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookPublishedException extends GuidebookException {

  public GuidebookPublishedException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_PUBLISHED, message);
  }

  public static GuidebookPublishedException withId(UUID id) {
    return new GuidebookPublishedException(id + "출판된 가이드북은 삭제될 수 없습니다.");
  }

}
