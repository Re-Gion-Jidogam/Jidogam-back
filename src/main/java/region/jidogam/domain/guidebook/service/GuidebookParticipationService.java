package region.jidogam.domain.guidebook.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.exp.service.ExpService;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookParticipation;
import region.jidogam.domain.guidebook.repository.GuidebookParticipationRepository;
import region.jidogam.domain.guidebook.repository.GuidebookPlaceRepository;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuidebookParticipationService {

  private final GuidebookParticipationRepository guidebookParticipationRepository;
  private final GuidebookPlaceRepository guidebookPlaceRepository;
  private final ExpService expService;
  private final UserService userService;

  /**
   * 스탬프 생성 시 가이드북 참여 진행상황 업데이트
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void updateProgressByStamp(User user, Place place) {
    LocalDateTime activityTime = LocalDateTime.now();

    // 1. 유저가 진행중인 가이드북 참여 목록 조회
    List<GuidebookParticipation> participations =
        guidebookParticipationRepository.findInProgressByUserId(user.getId());

    if (participations.isEmpty()) {
      return;
    }

    // 2. 해당 장소를 포함하는 가이드북 필터링
    List<UUID> guidebookIds = participations.stream()
        .map(p -> p.getGuidebook().getId())
        .toList();

    Set<UUID> guidebookIdsContainingPlace =
        guidebookPlaceRepository.findGuidebookIdsByPlaceIdAndGuidebookIds(
            place.getId(), guidebookIds);

    // 3. 참여 목록 업데이트
    participations.stream()
        .filter(p -> guidebookIdsContainingPlace.contains(p.getGuidebook().getId()))
        .forEach(participation -> {
          updateProgressByStamp(participation, place.getExp(), activityTime);
          checkAndCompleteGuidebook(participation, user, activityTime);
        });
  }

  private void updateProgressByStamp(GuidebookParticipation participation, int placeExp,
      LocalDateTime activityTime) {
    participation.addEarnedExp(placeExp);
    participation.incrementCompletedPlaceCount();
    participation.updateLastActivityAt(activityTime);
  }

  private void checkAndCompleteGuidebook(GuidebookParticipation participation, User user,
      LocalDateTime activityTime) {

    Guidebook guidebook = participation.getGuidebook();

    if (participation.getCompletedPlaceCount() >= guidebook.getTotalPlaceCount()) {
      participation.markAsCompleted(activityTime);

      int completionExp = expService.calculateGuidebookCompletionExp(participation.getEarnedExp());

      userService.increaseUserExp(user, completionExp);

      log.info(
          "가이드북 완료 보상: userId = {}, guidebookId = {}, earnedExp = {}, completionExp = {}",
          user.getId(), guidebook.getId(), participation.getEarnedExp(), completionExp
      );
    }
  }
}
