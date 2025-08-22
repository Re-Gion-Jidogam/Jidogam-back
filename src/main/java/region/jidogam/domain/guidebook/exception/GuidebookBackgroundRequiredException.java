package region.jidogam.domain.guidebook.exception;

public class GuidebookBackgroundRequiredException extends GuidebookException {

  public GuidebookBackgroundRequiredException() {
    super(GuidebookErrorCode.GUIDEBOOK_BACKGROUND_REQUIRED);
  }

  public GuidebookBackgroundRequiredException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_BACKGROUND_REQUIRED, message);
  }

  public static GuidebookBackgroundRequiredException required() {
    return new GuidebookBackgroundRequiredException();
  }
}
