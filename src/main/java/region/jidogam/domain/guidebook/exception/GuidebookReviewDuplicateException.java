package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookReviewDuplicateException extends GuidebookException {

  public GuidebookReviewDuplicateException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_REVIEW_DUPLICATE, message);
  }

  public static GuidebookReviewDuplicateException withId(UUID id) {
    return new GuidebookReviewDuplicateException(id + " 가이드북에 이미 리뷰를 작성했습니다.");
  }
}