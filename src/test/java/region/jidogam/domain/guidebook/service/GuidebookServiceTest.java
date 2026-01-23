package region.jidogam.domain.guidebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.common.util.CursorCodecUtil;
import region.jidogam.domain.File.storage.FileStorage;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.exp.service.ExpService;
import region.jidogam.domain.guidebook.dto.AreaRatioDto;
import region.jidogam.domain.guidebook.dto.GuidebookAddPlaceRequest;
import region.jidogam.domain.guidebook.dto.GuidebookConditionRequest;
import region.jidogam.domain.guidebook.dto.GuidebookCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookCursor;
import region.jidogam.domain.guidebook.dto.GuidebookFilter;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.dto.GuidebookSortBy;
import region.jidogam.domain.guidebook.dto.GuidebookUpdateRequest;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookParticipation;
import region.jidogam.domain.guidebook.entity.GuidebookPlace;
import region.jidogam.domain.guidebook.exception.AuthorMismatchException;
import region.jidogam.domain.guidebook.exception.GuidebookAlreadyParticipatedException;
import region.jidogam.domain.guidebook.exception.GuidebookBackgroundRequiredException;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.exception.GuidebookNotPublishedException;
import region.jidogam.domain.guidebook.exception.GuidebookPublishConditionException;
import region.jidogam.domain.guidebook.exception.GuidebookPublishedException;
import region.jidogam.domain.guidebook.exception.GuidebookUnpublishViolationException;
import region.jidogam.domain.guidebook.mapper.GuidebookMapper;
import region.jidogam.domain.guidebook.repository.GuidebookAreaRatioRepository;
import region.jidogam.domain.guidebook.repository.GuidebookParticipationRepository;
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

  private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

  @Mock
  private UserRepository userRepository;
  @Mock
  private GuidebookRepository guidebookRepository;
  @Mock
  private GuidebookPlaceRepository guidebookPlaceRepository;
  @Mock
  private GuidebookParticipationRepository guidebookParticipantRepository;
  @Mock
  private GuidebookAreaRatioRepository guidebookAreaRatioRepository;
  @Mock
  private StampRepository stampRepository;
  @Mock
  private PlaceRepository placeRepository;
  @Mock
  private PlaceService placeService;
  @Mock
  private ExpService expService;
  @Mock
  private CursorCodecUtil cursorCodecUtil;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Spy
  private FileStorage fileStorage;

  private GuidebookMapper guidebookMapper;
  private GuidebookService guidebookService;

  @BeforeEach
  void setUp() {
    guidebookMapper = Mockito.spy(new GuidebookMapper(fileStorage));

    guidebookService = new GuidebookService(
        userRepository,
        guidebookRepository,
        guidebookPlaceRepository,
        guidebookParticipantRepository,
        guidebookAreaRatioRepository,
        stampRepository,
        placeRepository,
        placeService,
        expService,
        guidebookMapper,
        cursorCodecUtil,
        eventPublisher
    );
  }

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
      assertThat(result.visitedPlaceCount()).isEqualTo(expectedResponse.visitedPlaceCount());
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

      Guidebook guidebook = createGuidebook(userId, guidebookId, 0);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId)).thenReturn(0);

      GuidebookUpdateRequest request = new GuidebookUpdateRequest(
          "제목 수정",
          "설명 수정",
          null,
          null,
          null,
          null
      );

      // when
      GuidebookResponse response = guidebookService.update(guidebookId, userId, request);

      // then
      assertThat(response.title()).isEqualTo("제목 수정");
      assertThat(response.description()).isEqualTo("설명 수정");
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

    // 출판 시 엣지 조건 확인
    @Test
    @DisplayName("장소가 없는 가이드북 출판 시 예외 발생")
    void failsByNoPlaces() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Guidebook guidebook = createGuidebook(userId, guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(guidebookPlaceRepository.findAreasByPlaceCountDesc(any(), any()))
          .thenReturn(List.of());

      // when & then
      assertThrows(GuidebookPublishConditionException.class,
          () -> guidebookService.update(guidebookId, userId,
              new GuidebookUpdateRequest(null, null, null, null, null, true)));
    }

    @ParameterizedTest
    @CsvSource({
        // totalCount, firstPlaceCount, isLocal
        "9, 7, true",     // 9개 중 7개 = 77.78% → Local (70% 이상)
        "9, 6, false",    // 9개 중 6개 = 66.67% → Not Local (70% 미만)
        "10, 6, true",    // 10개 중 6개 = 60.0% → Local (60% 이상)
        "10, 5, false",   // 10개 중 5개 = 50.0% → Not Local (60% 미만)
        "30, 15, true",   // 30개 중 15개 = 50.0% → Local (50% 이상)
        "30, 14, false"   // 30개 중 14개 = 46.67% → Not Local (50% 미만)
    })
    @DisplayName("장소 개수와 1위 지역 비율에 따른 Local 가이드북 판단")
    void determineLocalGuidebook(int totalCount, long firstPlaceCount, boolean expectedLocal) {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Guidebook guidebook = createGuidebook(userId, guidebookId, totalCount);
      List<AreaRatioDto> areas = List.of(
          new AreaRatioDto(mock(Area.class), firstPlaceCount, 0.0),
          new AreaRatioDto(mock(Area.class), totalCount - firstPlaceCount, 0.0)
      );

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(guidebookPlaceRepository.findAreasByPlaceCountDesc(guidebookId, PageRequest.of(0, 3)))
          .thenReturn(areas);

      // when
      guidebookService.update(guidebookId, userId,
          new GuidebookUpdateRequest(null, null, null, null, null, true));

      // then
      verify(guidebookAreaRatioRepository).save(argThat(ratio ->
          ratio.getIsPrimaryArea() == expectedLocal
      ));
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
      when(guidebookPlaceRepository.existsByGuidebookAndPlace(any(), any())).thenReturn(false);
      when(mockGuidebook.getId()).thenReturn(guidebookId);
      when(mockGuidebook.getAuthor()).thenReturn(user);

      when(placeService.getOrCreatePlace(request.pid(), request.place())).thenReturn(mockPlace);
      when(mockPlace.getId()).thenReturn(placeId);
      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId)).thenReturn(3);
      when(guidebookMapper.toResponse(mockGuidebook, 3)).thenReturn(expectedResponse);

      // when
      GuidebookResponse response = guidebookService.addPlace(guidebookId, userId, request);

      // then
      verify(guidebookPlaceRepository).save(any(GuidebookPlace.class));
      verify(mockGuidebook).updateMapImageUrl("https://test.com/url");
      verify(mockGuidebook).increaseTotalPlaceCount();
      verify(placeRepository).updateGuidebookCount(placeId, 1);

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
      when(guidebookPlaceRepository.deleteByGuidebook_IdAndPlace_Id(guidebookId, placeId))
          .thenReturn(1);

      // when
      guidebookService.removePlace(guidebookId, placeId, userId);

      // then
      verify(mockGuidebook).decreaseTotalPlaceCount();
      verify(placeRepository).updateGuidebookCount(placeId, -1);
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

      GuidebookParticipation guidebookParticipation = GuidebookParticipation.builder()
          .guidebook(guidebook)
          .user(user)
          .build();

      // when
      guidebookService.addParticipant(guidebookId, userId);

      // then
      verify(guidebookParticipantRepository).save(any(GuidebookParticipation.class));
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

  @Nested
  @DisplayName("장소(placeId)별 가이드북 목록 조회")
  class ListByPlaceId {

    @Test
    @DisplayName("장소별 가이드북 목록 조회 성공 - 다음 페이지 있음")
    void successWithNextPage() {
      // given
      UUID placeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID guidebookId1 = UUID.randomUUID();
      UUID guidebookId2 = UUID.randomUUID();
      UUID guidebookId3 = UUID.randomUUID();

      GuidebookConditionRequest request = new GuidebookConditionRequest(null,
          GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 2, null);

      Guidebook guidebook1 = createGuidebook(userId, guidebookId1);
      Guidebook guidebook2 = createGuidebook(userId, guidebookId2);
      Guidebook guidebook3 = createGuidebook(userId, guidebookId3);

      GuidebookResponse response1 = createResponse(userId, guidebookId1, 5);
      GuidebookResponse response2 = createResponse(userId, guidebookId2, 3);

      when(cursorCodecUtil.decodeGuidebookCursor(null, GuidebookSortBy.CREATED_AT))
          .thenReturn(null);

      when(guidebookRepository.searchGuidebooksByPlaceId(placeId, null, null,
          GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 3))
          .thenReturn(new ArrayList<>(List.of(guidebook1, guidebook2, guidebook3)));
      when(guidebookRepository.countGuidebooksByPlaceId(placeId, null, null))
          .thenReturn(5L);

      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId1)).thenReturn(5);
      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId2)).thenReturn(3);

      when(guidebookMapper.toResponse(guidebook1, 5)).thenReturn(response1);
      when(guidebookMapper.toResponse(guidebook2, 3)).thenReturn(response2);

      when(cursorCodecUtil.encodeNextCursor(response2, GuidebookSortBy.CREATED_AT))
          .thenReturn("nextCursorValue");

      // when
      CursorPageResponseDto<GuidebookResponse> result = guidebookService.listByPlaceId(
          placeId, request, userId);

      // then
      assertThat(result.data()).hasSize(2);
      assertThat(result.hasNext()).isTrue();
      assertThat(result.nextCursor()).isEqualTo("nextCursorValue");
      assertThat(result.totalCount()).isEqualTo(5L);
      assertThat(result.sortBy()).isEqualTo("createdAt");
    }

    @Test
    @DisplayName("장소별 가이드북 목록 조회 성공 - 다음 페이지 없음")
    void successWithoutNextPage() {
      // given
      UUID placeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID guidebookId1 = UUID.randomUUID();

      GuidebookConditionRequest request = new GuidebookConditionRequest(
          null, GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 10, null
      );

      Guidebook guidebook1 = createGuidebook(userId, guidebookId1);
      GuidebookResponse response1 = createResponse(userId, guidebookId1, 2);

      when(cursorCodecUtil.decodeGuidebookCursor(null, GuidebookSortBy.CREATED_AT))
          .thenReturn(null);

      when(guidebookRepository.searchGuidebooksByPlaceId(placeId, null, null,
          GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 11))
          .thenReturn(List.of(guidebook1));
      when(guidebookRepository.countGuidebooksByPlaceId(placeId, null, null))
          .thenReturn(1L);

      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId1)).thenReturn(2);

      when(guidebookMapper.toResponse(guidebook1, 2)).thenReturn(response1);

      // when
      CursorPageResponseDto<GuidebookResponse> result = guidebookService.listByPlaceId(
          placeId, request, userId);

      // then
      assertThat(result.data()).hasSize(1);
      assertThat(result.hasNext()).isFalse();
      assertThat(result.nextCursor()).isNull();
      assertThat(result.totalCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("장소별 가이드북 목록 조회 - 빈 결과")
    void successWithEmptyResult() {
      // given
      UUID placeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      GuidebookConditionRequest request = new GuidebookConditionRequest(
          null, GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 10, null
      );

      when(cursorCodecUtil.decodeGuidebookCursor(null, GuidebookSortBy.CREATED_AT))
          .thenReturn(null);

      when(guidebookRepository.searchGuidebooksByPlaceId(placeId, null, null,
          GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 11))
          .thenReturn(List.of());
      when(guidebookRepository.countGuidebooksByPlaceId(placeId, null, null))
          .thenReturn(0L);

      // when
      CursorPageResponseDto<GuidebookResponse> result = guidebookService.listByPlaceId(
          placeId, request, userId);

      // then
      assertThat(result.data()).isEmpty();
      assertThat(result.hasNext()).isFalse();
      assertThat(result.nextCursor()).isNull();
      assertThat(result.totalCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("LOCAL 필터 적용 시 isLocal=true로 조회")
    void successWithLocalFilter() {
      // given
      UUID placeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();

      GuidebookConditionRequest request = new GuidebookConditionRequest(
          GuidebookFilter.LOCAL, GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 10, null
      );

      Guidebook guidebook = createGuidebook(userId, guidebookId);
      GuidebookResponse response = createResponse(userId, guidebookId, 0);

      when(cursorCodecUtil.decodeGuidebookCursor(null, GuidebookSortBy.CREATED_AT))
          .thenReturn(null);

      when(guidebookRepository.searchGuidebooksByPlaceId(placeId, null, null,
          GuidebookSortBy.CREATED_AT, SortDirection.DESC, true, 11))
          .thenReturn(List.of(guidebook));
      when(guidebookRepository.countGuidebooksByPlaceId(placeId, null, true))
          .thenReturn(1L);

      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId)).thenReturn(0);

      when(guidebookMapper.toResponse(guidebook, 0)).thenReturn(response);

      // when
      CursorPageResponseDto<GuidebookResponse> result = guidebookService.listByPlaceId(
          placeId, request, userId);

      // then
      assertThat(result.data()).hasSize(1);
      verify(guidebookRepository).searchGuidebooksByPlaceId(
          placeId, null, null, GuidebookSortBy.CREATED_AT, SortDirection.DESC, true, 11);
      verify(guidebookRepository).countGuidebooksByPlaceId(placeId, null, true);
    }

    @Test
    @DisplayName("비로그인 사용자(userId=null)의 경우 visitedPlaceCount=0")
    void successWithNullUserId() {
      // given
      UUID placeId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();

      GuidebookConditionRequest request = new GuidebookConditionRequest(
          null, GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 10, null
      );

      Guidebook guidebook = createGuidebook(authorId, guidebookId);
      GuidebookResponse response = createResponse(authorId, guidebookId, 0);

      when(cursorCodecUtil.decodeGuidebookCursor(null, GuidebookSortBy.CREATED_AT))
          .thenReturn(null);

      when(guidebookRepository.searchGuidebooksByPlaceId(placeId, null, null,
          GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 11))
          .thenReturn(List.of(guidebook));
      when(guidebookRepository.countGuidebooksByPlaceId(placeId, null, null))
          .thenReturn(1L);

      when(guidebookMapper.toResponse(guidebook, 0)).thenReturn(response);

      // when
      CursorPageResponseDto<GuidebookResponse> result = guidebookService.listByPlaceId(
          placeId, request, null);

      // then
      assertThat(result.data()).hasSize(1);
      assertThat(result.data().get(0).visitedPlaceCount()).isEqualTo(0);
      verify(stampRepository, Mockito.never()).countUserStampsInGuidebook(any(), any());
    }

    @Test
    @DisplayName("키워드 검색 적용")
    void successWithKeyword() {
      // given
      UUID placeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();

      String keyword = "서울";
      GuidebookConditionRequest request = new GuidebookConditionRequest(
          null, GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 10, keyword
      );

      Guidebook guidebook = createGuidebook(userId, guidebookId);
      GuidebookResponse response = createResponse(userId, guidebookId, 1);

      when(cursorCodecUtil.decodeGuidebookCursor(null, GuidebookSortBy.CREATED_AT))
          .thenReturn(null);
      when(guidebookRepository.searchGuidebooksByPlaceId(
          placeId, null, keyword, GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 11))
          .thenReturn(List.of(guidebook));
      when(guidebookRepository.countGuidebooksByPlaceId(placeId, keyword, null))
          .thenReturn(1L);
      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId)).thenReturn(1);
      when(guidebookMapper.toResponse(guidebook, 1)).thenReturn(response);

      // when
      CursorPageResponseDto<GuidebookResponse> result = guidebookService.listByPlaceId(
          placeId, request, userId);

      // then
      assertThat(result.data()).hasSize(1);
      verify(guidebookRepository).searchGuidebooksByPlaceId(
          placeId, null, keyword, GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 11);
    }

    @Test
    @DisplayName("참여자 수 기준 정렬")
    void successSortByParticipantCount() {
      // given
      UUID placeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();

      GuidebookConditionRequest request = new GuidebookConditionRequest(
          null, GuidebookSortBy.PARTICIPANT_COUNT, SortDirection.DESC, null, 10, null
      );

      Guidebook guidebook = createGuidebook(userId, guidebookId);
      GuidebookResponse response = createResponse(userId, guidebookId, 0);

      when(cursorCodecUtil.decodeGuidebookCursor(null, GuidebookSortBy.PARTICIPANT_COUNT))
          .thenReturn(null);
      when(guidebookRepository.searchGuidebooksByPlaceId(
          placeId, null, null, GuidebookSortBy.PARTICIPANT_COUNT, SortDirection.DESC, null, 11))
          .thenReturn(List.of(guidebook));
      when(guidebookRepository.countGuidebooksByPlaceId(placeId, null, null))
          .thenReturn(1L);
      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId)).thenReturn(0);
      when(guidebookMapper.toResponse(guidebook, 0)).thenReturn(response);

      // when
      CursorPageResponseDto<GuidebookResponse> result = guidebookService.listByPlaceId(
          placeId, request, userId);

      // then
      assertThat(result.sortBy()).isEqualTo("participantCount");
      verify(guidebookRepository).searchGuidebooksByPlaceId(
          placeId, null, null, GuidebookSortBy.PARTICIPANT_COUNT, SortDirection.DESC, null, 11);
    }

    @Test
    @DisplayName("커서 기반 페이지네이션")
    void successWithCursor() {
      // given
      UUID placeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();

      String cursorValue = "encodedCursor";
      GuidebookCursor decodedCursor = new GuidebookCursor(null, FIXED_DATE_TIME, UUID.randomUUID());

      GuidebookConditionRequest request = new GuidebookConditionRequest(
          null, GuidebookSortBy.CREATED_AT, SortDirection.DESC, cursorValue, 10, null
      );

      Guidebook guidebook = createGuidebook(userId, guidebookId);
      GuidebookResponse response = createResponse(userId, guidebookId, 0);

      when(cursorCodecUtil.decodeGuidebookCursor(cursorValue, GuidebookSortBy.CREATED_AT))
          .thenReturn(decodedCursor);
      when(guidebookRepository.searchGuidebooksByPlaceId(
          placeId, decodedCursor, null, GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 11))
          .thenReturn(List.of(guidebook));
      when(guidebookRepository.countGuidebooksByPlaceId(placeId, null, null))
          .thenReturn(10L);
      when(stampRepository.countUserStampsInGuidebook(userId, guidebookId)).thenReturn(0);
      when(guidebookMapper.toResponse(guidebook, 0)).thenReturn(response);

      // when
      CursorPageResponseDto<GuidebookResponse> result = guidebookService.listByPlaceId(
          placeId, request, userId);

      // then
      assertThat(result.data()).hasSize(1);
      verify(cursorCodecUtil).decodeGuidebookCursor(cursorValue, GuidebookSortBy.CREATED_AT);
      verify(guidebookRepository).searchGuidebooksByPlaceId(
          placeId, decodedCursor, null, GuidebookSortBy.CREATED_AT, SortDirection.DESC, null, 11);
    }
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
    return createGuidebook(userId, guidebookId, 10);
  }

  private Guidebook createGuidebook(UUID userId, UUID guidebookId, int totalPlaceCount) {
    User user = createUser(userId);

    Guidebook guidebook = Guidebook.builder()
        .title("테스트 가이드북")
        .description("테스트용 가이드북 설명")
        .thumbnailUrl("https://test.com/url")
        .emoji("😭")
        .color("#12345")
        .exp(100)
        .publishedDate(FIXED_DATE_TIME)
        .mapImageUrl("https://test.com/url")
        .participantCount(3)
        .totalPlaceCount(totalPlaceCount)
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
        .exp(100)
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
