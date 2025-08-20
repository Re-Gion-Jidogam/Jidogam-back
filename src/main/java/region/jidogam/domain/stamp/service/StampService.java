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
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.exception.PlaceNotFoundException;
import region.jidogam.domain.place.repository.PlaceRepository;
import region.jidogam.domain.place.service.PlaceService;
import region.jidogam.domain.stamp.Repository.StampRepository;
import region.jidogam.domain.stamp.dto.PlaceStampRequest;
import region.jidogam.domain.stamp.entity.Stamp;
import region.jidogam.domain.stamp.exception.StampCooldownException;
import region.jidogam.domain.stamp.exception.StampDuplicateException;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;

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

  @Transactional
  public void stampPlace(PlaceStampRequest request, UUID userId) {
    log.info("장소 도장 찍기 시작: placeName = {}, userId = {}", request.place().placeName(), userId);

    // 1. 유저 확인
    User user = userRepository.findById(userId)
      .orElseThrow(() -> UserNotFoundException.withId(userId));

    // 2. 유저의 마지막 도장 찍은 시간 조회 (유효 시간 외인 경우 예외)
    validateStampCoolTime(user.getId());

    // 3. 장소 데이터 조회
    Place place;
    if (request.pid() != null) {
      place = handleExistingPlace(user, request);
    } else {
      place = handleNewPlace(request);
    }

    // 4. 도장
    Stamp stamp = Stamp.builder()
      .place(place)
      .user(user)
      .build();

    stampRepository.save(stamp);
    log.info("장소 도장 찍기 완료: placeName = {}, email = {}", request.place().placeName(),
      user.getEmail());
  }

  // 기존 장소
  private Place handleExistingPlace(User user, PlaceStampRequest request) {
    Place place = placeRepository.findById(request.pid())
      .orElseThrow(() -> PlaceNotFoundException.withPlaceName(request.place().placeName()));

    // 도장 중복 검사
    validateDuplicateStamp(user, place);

    return place;
  }

  // 새로운 장소
  private Place handleNewPlace(PlaceStampRequest request) {
    return placeService.createPlace(request.place());
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
