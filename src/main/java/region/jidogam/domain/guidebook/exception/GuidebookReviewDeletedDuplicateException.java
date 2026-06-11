package region.jidogam.domain.guidebook.exception;

import java.util.UUID;

public class GuidebookReviewDeletedDuplicateException extends GuidebookException {

  public GuidebookReviewDeletedDuplicateException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_REVIEW_DELETED_DUPLICATE, message);
  }

  public static GuidebookReviewDeletedDuplicateException withId(UUID guidebookId) {
    return new GuidebookReviewDeletedDuplicateException(
        guidebookId + " 가이드북에 삭제한 리뷰가 있어 재작성이 불가합니다.");
  }
}