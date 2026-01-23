package region.jidogam.domain.stamp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.entity.Area.AreaType;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.exception.PlaceNotFoundException;
import region.jidogam.domain.place.repository.PlaceRepository;
import region.jidogam.domain.place.service.PlaceService;
import region.jidogam.domain.stamp.dto.PlaceStampRequest;
import region.jidogam.domain.stamp.entity.Stamp;
import region.jidogam.domain.stamp.exception.StampCooldownException;
import region.jidogam.domain.stamp.exception.StampDuplicateException;
import region.jidogam.domain.stamp.exception.StampNotFoundException;
import region.jidogam.domain.stamp.repository.StampRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StampServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private StampRepository stampRepository;

  @Mock
  private PlaceRepository placeRepository;

  @Mock
  private PlaceService placeService;

  @Mock
  private Clock clock;

  @InjectMocks
  private StampService stampService;

  private UUID userId;
  private User user;
  private UUID placeId;
  private Place place;
  private PlaceCreateRequest placeCreateRequest;
  private Area area;

  @BeforeEach
  void setUp() {
    // 테스트 쿨타임 5분
    ReflectionTestUtils.setField(stampService, "cooldownTime", Duration.ofMinutes(5));

    user = User.builder()
        .email("user@email.com")
        .nickname("users")
        .exp(0L)
        .password("testPassword")
        .profileImageUrl(null)
        .build();

    placeCreateRequest = new PlaceCreateRequest(
        null,
        "임시마트",
        "전북 익산시 망산길 11-17",
        null,
        BigDecimal.valueOf(35.976749396987046),
        BigDecimal.valueOf(126.99599512792346)
    );

    area = Area.builder()
        .sigunguCode("1234")
        .sido("전라북도특별자치도")
        .sigungu("익산시")
        .weight(1.0)
        .type(AreaType.NORMAL)
        .build();

    place = Place.builder()
        .name(placeCreateRequest.placeName())
        .address(placeCreateRequest.addressName())
        .x(placeCreateRequest.x())
        .y(placeCreateRequest.y())
        .category(placeCreateRequest.category())
        .area(area)
        .exp(10)
        .build();

    userId = UUID.randomUUID();
    placeId = UUID.randomUUID();
    ReflectionTestUtils.setField(user, "id", userId);
    ReflectionTestUtils.setField(place, "id", placeId);
  }

  @Test
  @DisplayName("이미 존재하는 장소인 경우 도장 찍기 성공")
  void alreadyExistsPlaceStampSuccess() {
    // given
    setClock();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId))
        .thenReturn(Optional.empty());
    when(placeService.getOrCreatePlace(placeId, placeCreateRequest)).thenReturn(place);
    when(stampRepository.existsByUser_IdAndPlace_Id(userId, place.getId()))
        .thenReturn(false);

    PlaceStampRequest request = new PlaceStampRequest(placeId, placeCreateRequest);

    // when
    stampService.stampPlace(request, userId);

    // then
    verify(placeService, never()).createPlace(request.place());

    // save가 호출될 때 인자를 캡처, 캡처된 Stamp 객체의 내용을 검증
    ArgumentCaptor<Stamp> stampCaptor = ArgumentCaptor.forClass(Stamp.class);
    verify(stampRepository).save(stampCaptor.capture());

    Stamp savedStamp = stampCaptor.getValue();
    assertThat(savedStamp.getUser()).isEqualTo(user);
    assertThat(savedStamp.getPlace()).isEqualTo(place);

    verify(placeRepository).updateStampCount(placeId, 1);
  }

  @Test
  @DisplayName("새로운 장소인 경우 도장 찍기 성공")
  void newPlaceStampSuccess() {
    // given
    setClock();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId))
        .thenReturn(Optional.empty());
    when(placeService.getOrCreatePlace(null, placeCreateRequest)).thenReturn(place);

    PlaceStampRequest request = new PlaceStampRequest(null, placeCreateRequest);

    // when
    stampService.stampPlace(request, userId);

    // then
    ArgumentCaptor<Stamp> stampCaptor = ArgumentCaptor.forClass(Stamp.class);
    verify(stampRepository).save(stampCaptor.capture());

    Stamp savedStamp = stampCaptor.getValue();
    assertThat(savedStamp.getUser()).isEqualTo(user);
    assertThat(savedStamp.getPlace()).isEqualTo(place);

    verify(placeRepository).updateStampCount(placeId, 1);
  }

  @Test
  @DisplayName("장소id로 장소 데이터를 찾을 수 없는 경우 실패")
  void failsByPlaceNotFound() {
    // given
    setClock();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId))
        .thenReturn(Optional.empty());
    when(placeService.getOrCreatePlace(placeId, placeCreateRequest)).thenThrow(
        PlaceNotFoundException.class);
    PlaceStampRequest request = new PlaceStampRequest(placeId, placeCreateRequest);

    // when & then
    assertThrows(PlaceNotFoundException.class,
        () -> stampService.stampPlace(request, userId));
  }

  @Test
  @DisplayName("이미 도장을 찍은 장소일 경우 실패")
  void failsByAlreadyStamp() {
    // given
    setClock();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId))
        .thenReturn(Optional.empty());
    when(placeService.getOrCreatePlace(placeId, placeCreateRequest)).thenReturn(place);
    when(stampRepository.existsByUser_IdAndPlace_Id(userId, placeId))
        .thenReturn(true);

    PlaceStampRequest request = new PlaceStampRequest(placeId, placeCreateRequest);

    // when & then
    assertThrows(StampDuplicateException.class,
        () -> stampService.stampPlace(request, userId));
  }

  @Nested
  @DisplayName("도장 쿨타임")
  class StampCoolDownTime {

    @Test
    @DisplayName("도장 쿨타임이 남은 경우 실패")
    void failsByCooldownTime() {
      // given
      setClock();
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      // CoolTime 5분, 마지막 도장 3분 전
      LocalDateTime recent = LocalDateTime.of(2025, 8, 20, 11, 57);
      Stamp stamp = Stamp.builder()
          .user(user)
          .place(place)
          .build();
      ReflectionTestUtils.setField(stamp, "createdAt", recent);
      when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId))
          .thenReturn(Optional.of(stamp));

      PlaceStampRequest request = new PlaceStampRequest(null, placeCreateRequest);

      // when & then
      assertThrows(StampCooldownException.class, () -> stampService.stampPlace(request, userId));
    }

    @Test
    @DisplayName("도장 쿨타임이 지난 경우 성공")
    void cooldownTimeIsAfterNowSuccess() {
      // given
      setClock();
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      // CoolTime 5분, 마지막 도장 10분 전
      LocalDateTime recent = LocalDateTime.of(2025, 8, 20, 11, 50);
      Stamp stamp = Stamp.builder()
          .user(user)
          .place(place)
          .build();
      ReflectionTestUtils.setField(stamp, "createdAt", recent);
      when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId))
          .thenReturn(Optional.of(stamp));

      when(placeService.getOrCreatePlace(null, placeCreateRequest)).thenReturn(place);

      PlaceStampRequest request = new PlaceStampRequest(null, placeCreateRequest);

      // when
      stampService.stampPlace(request, userId);

      // then
      ArgumentCaptor<Stamp> stampCaptor = ArgumentCaptor.forClass(Stamp.class);
      verify(stampRepository).save(stampCaptor.capture());

      Stamp savedStamp = stampCaptor.getValue();
      assertThat(savedStamp.getUser()).isEqualTo(user);
      assertThat(savedStamp.getPlace()).isEqualTo(place);

      verify(placeRepository).updateStampCount(placeId, 1);
    }
  }

  @Test
  @DisplayName("도장 삭제 성공")
  void cancelStampSuccess() {
    // given
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(stampRepository.deleteByUser_IdAndPlace_Id(userId, placeId)).thenReturn(1);

    // when
    stampService.cancelStamp(userId, placeId);

    // then
    verify(userRepository).findById(userId);
    verify(stampRepository).deleteByUser_IdAndPlace_Id(userId, placeId);
    verify(placeRepository).updateStampCount(placeId, -1);
  }

  @Test
  @DisplayName("도장이 없으면 예외 발생")
  void failsCancelStampByStampNotFound() {
    // given
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(stampRepository.deleteByUser_IdAndPlace_Id(userId, placeId)).thenReturn(0);

    // when & then
    assertThrows(StampNotFoundException.class, () -> stampService.cancelStamp(userId, placeId));

  }

  private void setClock() {
    ZoneId zone = ZoneId.of("Asia/Seoul");
    Instant fixed = LocalDateTime.of(2025, 8, 20, 12, 0).atZone(zone).toInstant();
    when(clock.instant()).thenReturn(fixed);
    when(clock.getZone()).thenReturn(zone);
  }
}
