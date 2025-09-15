package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookAlreadyParticipatedException extends GuidebookException {

  public GuidebookAlreadyParticipatedException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_ALREADY_PARTICIPATED, message);
  }

  public static GuidebookAlreadyParticipatedException withId(UUID id) {
    return new GuidebookAlreadyParticipatedException(id + " 가이드북에 이미 참여중입니다.");
  }
}
