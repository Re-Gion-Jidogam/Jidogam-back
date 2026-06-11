package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookReviewNotFoundException extends GuidebookException {

  public GuidebookReviewNotFoundException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_REVIEW_NOT_FOUND, message);
  }

  public static GuidebookReviewNotFoundException withId(UUID id) {
    return new GuidebookReviewNotFoundException(id + " 리뷰가 존재하지 않습니다.");
  }
}