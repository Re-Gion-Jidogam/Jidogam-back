package region.jidogam.domain.guidebook.exception;

public class ParticipationPlaceCountException extends GuidebookException {

  public ParticipationPlaceCountException(String message) {
    super(GuidebookErrorCode.GUIDEBOOK_PARTICIPATE_PLACE_COUNT, message);
  }

  public static ParticipationPlaceCountException invalidPlaceCount() {
    return new ParticipationPlaceCountException("참여자의 완료된 장소 수가 이미 0입니다.");
  }
}
