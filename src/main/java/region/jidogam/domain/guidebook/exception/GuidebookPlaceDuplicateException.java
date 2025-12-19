package region.jidogam.domain.guidebook.exception;

public class GuidebookPlaceDuplicateException extends GuidebookException {

  public GuidebookPlaceDuplicateException() {
    super(GuidebookErrorCode.GUIDEBOOK_PLACE_DUPLICATE);
  }

  public static GuidebookPlaceDuplicateException duplicate() {
    return new GuidebookPlaceDuplicateException();
  }
}
