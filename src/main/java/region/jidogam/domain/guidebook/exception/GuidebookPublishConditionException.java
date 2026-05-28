package region.jidogam.domain.guidebook.exception;

public class GuidebookPublishConditionException extends GuidebookException {

  public GuidebookPublishConditionException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_NOT_PUBLISHABLE, message);
  }

  public static GuidebookPublishConditionException insufficientPlaces(int min) {
    return new GuidebookPublishConditionException(
        "가이드북은 최소 " + min + "개 이상의 장소가 있어야 출판할 수 있습니다.");
  }
}
