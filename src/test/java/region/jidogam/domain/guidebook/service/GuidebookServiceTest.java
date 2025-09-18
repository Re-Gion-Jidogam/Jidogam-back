package region.jidogam.domain.guidebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.domain.guidebook.dto.GuidebookAddPlaceRequest;
import region.jidogam.domain.guidebook.dto.GuidebookCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.dto.GuidebookUpdateRequest;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookParticipant;
import region.jidogam.domain.guidebook.entity.GuidebookPlace;
import region.jidogam.domain.guidebook.exception.AuthorMismatchException;
import region.jidogam.domain.guidebook.exception.GuidebookAlreadyParticipatedException;
import region.jidogam.domain.guidebook.exception.GuidebookBackgroundRequiredException;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.exception.GuidebookNotPublishedException;
import region.jidogam.domain.guidebook.exception.GuidebookPublishedException;
import region.jidogam.domain.guidebook.exception.GuidebookUnpublishViolationException;
import region.jidogam.domain.guidebook.mapper.GuidebookMapper;
import region.jidogam.domain.guidebook.repository.GuidebookParticipantRepository;
import region.jidogam.domain.guidebook.repository.GuidebookPlaceRepository;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.repository.PlaceRepository;
import region.jidogam.domain.place.service.PlaceService;
import region.jidogam.domain.stamp.repository.StampRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GuidebookServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private GuidebookRepository guidebookRepository;

  @Mock
  private GuidebookPlaceRepository guidebookPlaceRepository;

  @Mock
  private GuidebookParticipantRepository guidebookParticipantRepository;

  @Mock
  private StampRepository stampRepository;

  @Mock
  private PlaceRepository placeRepository;

  @Mock
  private PlaceService placeService;

  @Spy
  private GuidebookMapper guidebookMapper;

  @InjectMocks
  private GuidebookService guidebookService;

  private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

  @Nested
  @DisplayName("가이드북 생성")
  class Create {

    @Test
    @DisplayName("가이드북 저장 성공")
    void success() {
      // given
      UUID userId = UUID.randomUUID();
      User user = mock(User.class);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      GuidebookCreateRequest request = new GuidebookCreateRequest(
        "title",
        "description",
        null,
        null,
        "url"
      );

      // when
      guidebookService.create(request, userId);

      // then
      verify(guidebookRepository).save(any(Guidebook.class));
    }

    @Test
    @DisplayName("배경 데이터가 하나도 없는 경우 예외 발생")
    void failsWhenBackgroundIsMissing() {
      // given
      UUID userId = UUID.randomUUID();
      User user = mock(User.class);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      GuidebookCreateRequest request = new GuidebookCreateRequest(
        "title",
        "description",
        null,
        null,
        null
      );

      // when & then
      assertThrows(GuidebookBackgroundRequiredException.class,
        () -> guidebookService.create(request, userId));

    }

    @Test
    @DisplayName("배경 색깔없이 이모지만 있는 경우 예외 발생")
    void failsWhenOnlyEmojiProvided() {
      // given
      UUID userId = UUID.randomUUID();
      User user = mock(User.class);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      GuidebookCreateRequest request = new GuidebookCreateRequest(
        "title",
        "description",
        "#emoji",
        null,
        null
      );

      // when & then
      assertThrows(GuidebookBackgroundRequiredException.class,
        () -> guidebookService.create(request, userId));
    }
  }

  @Nested
  @DisplayName("가이드북 상세 조회")
  class Get {

    @Test
    @DisplayName("가이드북 상세 조회")
    void successGet() {
      // given
      UUID userId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();

      Guidebook guidebook = createGuidebook(userId, guidebookId);
      GuidebookResponse expectedResponse = createResponse(userId, guidebookId, 3);

      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId)).thenReturn(3);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(guidebookMapper.toResponse(guidebook, 3)).thenReturn(expectedResponse);

      // when
      GuidebookResponse result = guidebookService.getById(guidebookId, userId);

      // then
      assertThat(result).isEqualTo(expectedResponse);
      verify(guidebookRepository).findById(guidebookId);
      verify(guidebookMapper).toResponse(guidebook, 3);
    }

    @Test
    @DisplayName("가이드북 존재하지 않는 경우 예외 발생")
    void failsByNotExists() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(GuidebookNotFoundException.class,
        () -> guidebookService.getById(guidebookId, userId));
    }
  }

  @Nested
  @DisplayName("가이드북 수정")
  class Update {

    @Test
    @DisplayName("가이드북 수정 성공")
    void successUpdate() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Guidebook guidebook = createGuidebook(userId, guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId)).thenReturn(0);

      GuidebookUpdateRequest request = new GuidebookUpdateRequest(
        "제목 수정",
        "설명 수정",
        null,
        null,
        null,
        true
      );

      // when
      GuidebookResponse response = guidebookService.update(guidebookId, userId, request);

      // then
      assertThat(response.title()).isEqualTo("제목 수정");
      assertThat(response.description()).isEqualTo("설명 수정");
      assertThat(response.publishedDate()).isNotNull();
    }

    @Test
    @DisplayName("가이드북에 이미 참여자가 존재하는 경우, 출판 취소 불가능 예외 발생")
    void failsByParticipantsExist() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Guidebook mockGuidebook = mock(Guidebook.class);
      User mockUser = mock(User.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(mockGuidebook));
      when(mockGuidebook.getAuthor()).thenReturn(mockUser);
      when(mockUser.getId()).thenReturn(userId);
      when(mockGuidebook.getParticipantCount()).thenReturn(1);

      GuidebookUpdateRequest request = new GuidebookUpdateRequest(
        null,
        null,
        null,
        null,
        null,
        false
      );

      // when & then
      assertThrows(GuidebookUnpublishViolationException.class,
        () -> guidebookService.update(guidebookId, userId, request));
    }
  }

  @Nested
  @DisplayName("가이드북 삭제")
  class Delete {

    @Test
    @DisplayName("가이드북 삭제 성공")
    void successDelete() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Guidebook guidebook = createGuidebook(userId, guidebookId);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      // when
      guidebookService.delete(guidebookId, userId);

      // then
      verify(guidebookPlaceRepository).deleteByGuidebook(guidebook);
      verify(guidebookRepository).delete(guidebook);
    }

    @Test
    @DisplayName("출판된 경우 삭제 실패 예외 발생")
    void failsByIsPublished() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Guidebook guidebook = createGuidebook(userId, guidebookId);
      guidebook.publish();

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      // when & then
      assertThrows(GuidebookPublishedException.class,
        () -> guidebookService.delete(guidebookId, userId));
    }
  }

  @Nested
  @DisplayName("가이드북 장소 추가")
  class AddPlace {

    @Test
    @DisplayName("가이드북 장소 추가")
    void successAddPlace() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID placeId = UUID.randomUUID();

      Place mockPlace = mock(Place.class);
      Guidebook mockGuidebook = mock(Guidebook.class);

      User user = createUser(userId);
      GuidebookResponse expectedResponse = createResponse(userId, guidebookId, 3);

      PlaceCreateRequest placeCreateRequest = new PlaceCreateRequest(
        null,
        "임시마트",
        "전북 익산시 망산길 11-17",
        null,
        BigDecimal.valueOf(35.976749396987046),
        BigDecimal.valueOf(126.99599512792346)
      );

      GuidebookAddPlaceRequest request = new GuidebookAddPlaceRequest(
        placeId,
        placeCreateRequest,
        "https://test.com/url"
      );

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(mockGuidebook));
      when(mockGuidebook.getId()).thenReturn(guidebookId);
      when(mockGuidebook.getAuthor()).thenReturn(user);

      when(placeService.getOrCreatePlace(request.pid(), request.place())).thenReturn(mockPlace);
      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId)).thenReturn(3);
      when(guidebookMapper.toResponse(mockGuidebook, 3)).thenReturn(expectedResponse);

      // when
      GuidebookResponse response = guidebookService.addPlace(guidebookId, userId, request);

      // then
      verify(guidebookPlaceRepository).save(any(GuidebookPlace.class));
      verify(mockGuidebook).updateMapImageUrl("https://test.com/url");
      verify(mockGuidebook).increaseTotalPlaceCount();

    }

    @Test
    @DisplayName("가이드북 작성자가 아닌 사용자가 장소 추가를 시도할 경우 예외 발생")
    void failsByNotAuthor() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      UUID anotherId = UUID.randomUUID();

      User user = createUser(authorId);

      Guidebook mockGuidebook = mock(Guidebook.class);
      GuidebookAddPlaceRequest mockRequest = mock(GuidebookAddPlaceRequest.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(mockGuidebook));
      when(mockGuidebook.getAuthor()).thenReturn(user);

      // when & then
      assertThrows(AuthorMismatchException.class,
        () -> guidebookService.addPlace(guidebookId, anotherId, mockRequest));
    }

    @Test
    @DisplayName("가이드북이 출판된 경우 장소 추가 요청 시 예외 발생")
    void failsByAlreadyPublished() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();

      Guidebook guidebook = createGuidebook(authorId, guidebookId);
      guidebook.publish();

      GuidebookAddPlaceRequest mockRequest = mock(GuidebookAddPlaceRequest.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      // when & then
      assertThrows(GuidebookPublishedException.class,
        () -> guidebookService.addPlace(guidebookId, authorId, mockRequest));
    }
  }

  @Nested
  @DisplayName("가이드북 장소 제거")
  class removePlace {

    @Test
    @DisplayName("장소 제거 성공")
    void success() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID placeId = UUID.randomUUID();

      Guidebook mockGuidebook = mock(Guidebook.class);
      User mockUser = mock(User.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(mockGuidebook));
      when(mockGuidebook.getAuthor()).thenReturn(mockUser);
      when(mockUser.getId()).thenReturn(userId);
      when(
        guidebookPlaceRepository.deleteByGuidebook_IdAndPlace_Id(guidebookId, placeId)).thenReturn(
        1);

      // when
      guidebookService.removePlace(guidebookId, placeId, userId);

      // then
      verify(mockGuidebook).decreaseTotalPlaceCount();
    }

    @Test
    @DisplayName("작성자가 아닌 경우 장소 삭제 시 예외 발생")
    void failsByNotAuthor() {
      // given
      UUID userId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();

      UUID guidebookId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(authorId, guidebookId);

      UUID placeId = UUID.randomUUID();

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      // when & then
      assertThrows(AuthorMismatchException.class,
        () -> guidebookService.removePlace(guidebookId, placeId, userId));
    }
  }

  @Nested
  @DisplayName("가이드북 참여")
  class AddParticipant {

    @Test
    @DisplayName("가이드북 참여 성공")
    void success() {
      // given
      UUID userId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();

      User user = createUser(userId);
      Guidebook guidebook = createGuidebook(guidebookId, userId);
      guidebook.publish();

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(guidebookParticipantRepository.existsByGuidebookAndUser(guidebook, user))
        .thenReturn(false);

      GuidebookParticipant guidebookParticipant = GuidebookParticipant.builder()
        .guidebook(guidebook)
        .user(user)
        .build();

      // when
      guidebookService.addParticipant(guidebookId, userId);

      // then
      verify(guidebookParticipantRepository).save(any(GuidebookParticipant.class));
      verify(guidebookRepository).updateParticipantCount(guidebookId, 1);
    }

    @Test
    @DisplayName("출판되지 않은 경우 예외 발생")
    void failsByNotPublished() {
      // given
      UUID userId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();

      User user = createUser(userId);
      Guidebook guidebook = createGuidebook(guidebookId, userId);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      // when & then
      assertThrows(GuidebookNotPublishedException.class,
        () -> guidebookService.addParticipant(guidebookId, userId));

    }

    @Test
    @DisplayName("이미 참여중인 경우 예외 발생")
    void failsByAlreadyParticipated() {
      // given
      UUID userId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();

      User user = createUser(userId);
      Guidebook guidebook = createGuidebook(guidebookId, userId);
      guidebook.publish();

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(guidebookParticipantRepository.existsByGuidebookAndUser(guidebook, user))
        .thenReturn(true);

      // when & then
      assertThrows(GuidebookAlreadyParticipatedException.class,
        () -> guidebookService.addParticipant(guidebookId, userId));
    }
  }

  @Test
  @DisplayName("가이드북 참여 취소 성공")
  void successCancelParticipation() {
    // given
    UUID userId = UUID.randomUUID();
    UUID guidebookId = UUID.randomUUID();

    when(guidebookParticipantRepository.deleteByGuidebook_IdAndUser_Id(guidebookId, userId))
      .thenReturn(1);

    // when
    guidebookService.cancelParticipation(guidebookId, userId);

    // then
    verify(guidebookRepository).updateParticipantCount(guidebookId, -1);
  }


  /***
   * 이하 편의 클래스
   */
  private User createUser(UUID userId) {
    User user = User.builder()
      .nickname("testName")
      .password("password")
      .email("test@test.com")
      .build();

    ReflectionTestUtils.setField(user, "id", userId);

    return user;
  }

  private Guidebook createGuidebook(UUID userId, UUID guidebookId) {
    User user = createUser(userId);

    Guidebook guidebook = Guidebook.builder()
      .title("테스트 가이드북")
      .description("테스트용 가이드북 설명")
      .thumbnailUrl("https://test.com/url")
      .emoji("😭")
      .color("#12345")
      .points(100)
      .publishedDate(FIXED_DATE_TIME)
      .mapImageUrl("https://test.com/url")
      .participantCount(3)
      .totalPlaceCount(10)
      .ratingSum(11L)
      .ratingCount(10)
      .author(user)
      .build();

    ReflectionTestUtils.setField(guidebook, "id", guidebookId);
    ReflectionTestUtils.setField(guidebook, "createdAt", FIXED_DATE_TIME);
    ReflectionTestUtils.setField(guidebook, "updatedAt", FIXED_DATE_TIME);

    return guidebook;
  }

  private GuidebookResponse createResponse(UUID userId, UUID guidebookId, int visitedPlaceCount) {

    return GuidebookResponse.builder()
      .gid(guidebookId)
      .title("테스트 가이드북")
      .description("테스트용 가이드북 설명")
      .thumbnailUrl("https://test.com/url")
      .emoji("😭")
      .color("#12345")
      .point(100)
      .createdAt(FIXED_DATE_TIME)
      .updatedAt(FIXED_DATE_TIME)
      .publishedDate(FIXED_DATE_TIME)
      .mapImageUrl("https://test.com/url")
      .score(1.1)
      .participantCount(3)
      .totalPlaceCount(10)
      .visitedPlaceCount(visitedPlaceCount)
      .author(new GuidebookResponse.AuthorDto(
        userId,
        "testName",
        null
      ))
      .build();
  }

}
