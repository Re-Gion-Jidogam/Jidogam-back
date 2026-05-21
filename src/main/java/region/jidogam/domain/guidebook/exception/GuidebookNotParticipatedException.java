package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookNotParticipatedException extends GuidebookException {

  public GuidebookNotParticipatedException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_NOT_PARTICIPATED, message);
  }

  public static GuidebookNotParticipatedException withId(UUID id) {
    return new GuidebookNotParticipatedException(id + " 가이드북에 참여하지 않았습니다.");
  }
}