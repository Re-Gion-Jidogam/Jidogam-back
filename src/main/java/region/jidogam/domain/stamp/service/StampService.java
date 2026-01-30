package region.jidogam.domain.stamp.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.guidebook.service.GuidebookParticipationService;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.repository.PlaceRepository;
import region.jidogam.domain.place.service.PlaceService;
import region.jidogam.domain.stamp.dto.PlaceStampRequest;
import region.jidogam.domain.stamp.entity.Stamp;
import region.jidogam.domain.stamp.exception.StampCooldownException;
import region.jidogam.domain.stamp.exception.StampDuplicateException;
import region.jidogam.domain.stamp.exception.StampNotFoundException;
import region.jidogam.domain.stamp.repository.StampRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;
import region.jidogam.domain.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StampService {

  @Value("${jidogam.stamp.cooldown.time}")
  private Duration cooldownTime;

  private final Clock clock;
  private final UserRepository userRepository;
  private final StampRepository stampRepository;
  private final PlaceRepository placeRepository;
  private final PlaceService placeService;
  private final UserService userService;
  private final GuidebookParticipationService guidebookParticipationService;

  @Transactional
  public void stampPlace(PlaceStampRequest request, UUID userId) {
    log.info("장소 도장 찍기 시작: placeName = {}, userId = {}", request.place().placeName(), userId);

    // 1. 유저 확인
    User user = getUserOrThrow(userId);

    // 2. 유저의 마지막 도장 찍은 시간 조회 (유효 시간 외인 경우 예외)
    validateStampCoolTime(user.getId());

    // 3. 장소 데이터 조회
    Place place = placeService.getOrCreatePlace(request.pid(), request.place());

    // 4. 중복 검사
    if (request.pid() != null) {
      validateDuplicateStamp(user, place);
    }

    // 4. 도장 생성
    Stamp stamp = Stamp.builder()
        .place(place)
        .user(user)
        .earnedExp(place.getExp())
        .build();
    stampRepository.save(stamp);

    // 5. 사용자 경험치 증가
    userService.increaseUserExp(user, place.getExp());

    // 6. 장소 방문 수 증가
    placeRepository.updateStampCount(place.getId(), 1);

    // 7. 가이드북 완료 확인
    guidebookParticipationService.updateProgressByStamp(user, place);

    log.info("장소 도장 찍기 완료: placeName = {}, email = {}",
        request.place().placeName(), user.getEmail());
  }

  @Transactional
  public void cancelStamp(UUID userId, UUID placeId) {
    log.info("장소 도장 취소 시작: userId = {}, placeId = {}", userId, placeId);

    getUserOrThrow(userId);

    int deleted = stampRepository.deleteByUser_IdAndPlace_Id(userId, placeId);
    if (deleted == 0) {
      throw StampNotFoundException.withPlaceIdAndUserId(userId, placeId);
    }
    placeRepository.updateStampCount(placeId, -1);
    log.info("장소 도장 취소 완료: userId = {}, placeId = {}", userId, placeId);
  }

  // TODO: 도장 수 조회 메서드 추가 및 캐시 적용 필요

  // 유저 확인
  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  // 도장 쿨타임 검사
  private void validateStampCoolTime(UUID userId) {
    LocalDateTime now = LocalDateTime.now(clock);
    stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId)
        .ifPresent(lastStamp -> {
          if (lastStamp.getCreatedAt().isAfter(now.minus(cooldownTime))) {
            throw StampCooldownException.withRestTime(cooldownTime.toMinutes());
          }
        });
  }

  // 도장 중복 검사
  private void validateDuplicateStamp(User user, Place place) {
    if (stampRepository.existsByUser_IdAndPlace_Id(user.getId(), place.getId())) {
      throw StampDuplicateException.withPlaceName(place.getName());
    }
  }
}
