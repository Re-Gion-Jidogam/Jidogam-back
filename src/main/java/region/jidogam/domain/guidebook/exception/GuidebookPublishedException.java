package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookPublishedException extends GuidebookException {

  public GuidebookPublishedException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_PUBLISHED, message);
  }

  public static GuidebookPublishedException forDeletion(UUID id) {
    return new GuidebookPublishedException(id + " 출판된 가이드북은 삭제될 수 없습니다.");
  }

  public static GuidebookPublishedException forPlaceAddition(UUID id) {
    return new GuidebookPublishedException(id + " 출판된 가이드북에는 장소를 추가할 수 없습니다.");
  }

}
