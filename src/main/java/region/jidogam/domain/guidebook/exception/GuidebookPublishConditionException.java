package region.jidogam.domain.guidebook.exception;

public class GuidebookPublishConditionException extends GuidebookException {

  public GuidebookPublishConditionException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_NOT_PUBLISHABLE, message);
  }

  public static GuidebookPublishConditionException noPlace() {
    return new GuidebookPublishConditionException("빈 가이드북은 출판할 수 없습니다.");
  }
}
