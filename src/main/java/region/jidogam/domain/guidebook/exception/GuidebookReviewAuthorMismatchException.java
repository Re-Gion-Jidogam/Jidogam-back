package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookReviewAuthorMismatchException extends GuidebookException {

  public GuidebookReviewAuthorMismatchException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_REVIEW_AUTHOR_MISMATCH, message);
  }

  public static GuidebookReviewAuthorMismatchException withId(UUID reviewId) {
    return new GuidebookReviewAuthorMismatchException(reviewId + " 리뷰 작성자가 아닙니다.");
  }
}