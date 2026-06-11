package region.jidogam.domain.guidebook.exception;

public class GuidebookReviewInsufficientVisitsException extends GuidebookException {

  public GuidebookReviewInsufficientVisitsException(int required, int actual) {
    super(GuidebookErrorCode.GUIDEBOOK_REVIEW_INSUFFICIENT_VISITS,
        "리뷰 작성을 위해 최소 " + required + "개의 장소를 방문해야 합니다. (현재 방문: " + actual + "개)");
  }
}