package region.jidogam.domain.guidebook.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.domain.guidebook.dto.GuidebookReviewCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookReviewResponse;
import region.jidogam.domain.guidebook.dto.GuidebookReviewUpdateRequest;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookParticipation;
import region.jidogam.domain.guidebook.entity.GuidebookReview;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.exception.GuidebookNotParticipatedException;
import region.jidogam.domain.guidebook.exception.GuidebookReviewAuthorMismatchException;
import region.jidogam.domain.guidebook.exception.GuidebookReviewDuplicateException;
import region.jidogam.domain.guidebook.exception.GuidebookReviewInsufficientVisitsException;
import region.jidogam.domain.guidebook.exception.GuidebookReviewNotFoundException;
import region.jidogam.domain.guidebook.mapper.GuidebookReviewMapper;
import region.jidogam.domain.guidebook.repository.GuidebookParticipationRepository;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.domain.guidebook.repository.GuidebookReviewRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GuidebookReviewServiceTest {

  @Mock
  private GuidebookRepository guidebookRepository;
  @Mock
  private GuidebookReviewRepository guidebookReviewRepository;
  @Mock
  private GuidebookParticipationRepository guidebookParticipationRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private GuidebookReviewMapper guidebookReviewMapper;
  @InjectMocks
  private GuidebookReviewService guidebookReviewService;

  @Nested
  @DisplayName("리뷰 생성")
  class Create {

    @Test
    @DisplayName("일반 가이드북(장소 11개 이상)에서 5개 이상 방문 시 리뷰 저장 성공")
    void success() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(4, "좋은 가이드북입니다.");

      Guidebook guidebook = createGuidebook(guidebookId, 11);
      User user = createUser(userId);
      GuidebookParticipation participation = createParticipation(guidebook, user, 5);
      GuidebookReview savedReview = mock(GuidebookReview.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.of(participation));
      when(guidebookReviewRepository.existsByGuidebook_IdAndAuthor_Id(guidebookId, userId))
          .thenReturn(false);
      when(guidebookReviewRepository.save(any(GuidebookReview.class))).thenReturn(savedReview);
      when(guidebookReviewMapper.toResponse(savedReview)).thenReturn(mock(GuidebookReviewResponse.class));

      // when
      guidebookReviewService.create(guidebookId, userId, request);

      // then
      verify(guidebookReviewRepository).save(any(GuidebookReview.class));
      verify(guidebookRepository).updateRating(guidebookId, request.rating(), 1);
    }

    @Test
    @DisplayName("소규모 가이드북(장소 10개 이하)에서 4개 이상 방문 시 리뷰 저장 성공")
    void successSmallGuidebook() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(5, "소규모 가이드북 리뷰입니다.");

      Guidebook guidebook = createGuidebook(guidebookId, 10);
      User user = createUser(userId);
      GuidebookParticipation participation = createParticipation(guidebook, user, 4);
      GuidebookReview savedReview = mock(GuidebookReview.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.of(participation));
      when(guidebookReviewRepository.existsByGuidebook_IdAndAuthor_Id(guidebookId, userId))
          .thenReturn(false);
      when(guidebookReviewRepository.save(any(GuidebookReview.class))).thenReturn(savedReview);
      when(guidebookReviewMapper.toResponse(savedReview)).thenReturn(mock(GuidebookReviewResponse.class));

      // when
      guidebookReviewService.create(guidebookId, userId, request);

      // then
      verify(guidebookReviewRepository).save(any(GuidebookReview.class));
      verify(guidebookRepository).updateRating(guidebookId, request.rating(), 1);
    }

    @Test
    @DisplayName("존재하지 않는 가이드북이면 예외 발생")
    void failsByNotExists() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(4, "내용");

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(GuidebookNotFoundException.class,
          () -> guidebookReviewService.create(guidebookId, userId, request));
    }

    @Test
    @DisplayName("존재하지 않는 사용자이면 예외 발생")
    void failsByUserNotFound() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(4, "내용");

      Guidebook guidebook = createGuidebook(guidebookId, 11);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class,
          () -> guidebookReviewService.create(guidebookId, userId, request));
    }

    @Test
    @DisplayName("가이드북에 참여하지 않은 사용자이면 예외 발생")
    void failsByNotParticipated() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(4, "내용");

      Guidebook guidebook = createGuidebook(guidebookId, 11);
      User user = createUser(userId);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.empty());

      // when & then
      assertThrows(GuidebookNotParticipatedException.class,
          () -> guidebookReviewService.create(guidebookId, userId, request));
    }

    @ParameterizedTest(name = "총 장소 {0}개, 방문 {1}개 → 방문 수 부족 예외")
    @CsvSource({
        "11, 4",
        "10, 3"
    })
    @DisplayName("방문한 장소 수가 부족하면 예외 발생")
    void failsByInsufficientVisits(int totalPlaceCount, int completedPlaceCount) {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(4, "내용");

      Guidebook guidebook = createGuidebook(guidebookId, totalPlaceCount);
      User user = createUser(userId);
      GuidebookParticipation participation = createParticipation(guidebook, user, completedPlaceCount);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.of(participation));

      // when & then
      assertThrows(GuidebookReviewInsufficientVisitsException.class,
          () -> guidebookReviewService.create(guidebookId, userId, request));
    }

    @Test
    @DisplayName("이미 리뷰를 작성한 경우 예외 발생")
    void failsByAlreadyReviewed() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(4, "내용");

      Guidebook guidebook = createGuidebook(guidebookId, 11);
      User user = createUser(userId);
      GuidebookParticipation participation = createParticipation(guidebook, user, 5);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.of(participation));
      when(guidebookReviewRepository.existsByGuidebook_IdAndAuthor_Id(guidebookId, userId))
          .thenReturn(true);

      // when & then
      assertThrows(GuidebookReviewDuplicateException.class,
          () -> guidebookReviewService.create(guidebookId, userId, request));

      verify(guidebookReviewRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("리뷰 수정")
  class Update {

    @Test
    @DisplayName("rating과 content 모두 수정 성공")
    void success() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewUpdateRequest request = new GuidebookReviewUpdateRequest(5, "수정된 내용");

      Guidebook guidebook = createGuidebook(guidebookId, 11);
      User user = createUser(userId);
      GuidebookReview review = createReview(reviewId, guidebook, user, 3, "기존 내용");

      when(guidebookReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
      when(guidebookReviewMapper.toResponse(review)).thenReturn(mock(GuidebookReviewResponse.class));

      // when
      guidebookReviewService.update(reviewId, userId, request);

      // then
      verify(guidebookRepository).updateRating(guidebookId, 2, 0);
      verify(guidebookReviewMapper).toResponse(review);
    }

    @Test
    @DisplayName("content만 수정 시 guidebook ratingSum 변경 없음")
    void successOnlyContent() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewUpdateRequest request = new GuidebookReviewUpdateRequest(null, "수정된 내용");

      Guidebook guidebook = createGuidebook(UUID.randomUUID(), 11);
      User user = createUser(userId);
      GuidebookReview review = createReview(reviewId, guidebook, user, 4, "기존 내용");

      when(guidebookReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
      when(guidebookReviewMapper.toResponse(review)).thenReturn(mock(GuidebookReviewResponse.class));

      // when
      guidebookReviewService.update(reviewId, userId, request);

      // then
      verify(guidebookRepository, never()).updateRating(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("기존과 동일한 rating 전송 시 guidebook ratingSum 변경 없음")
    void successSameRating() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewUpdateRequest request = new GuidebookReviewUpdateRequest(4, null);

      Guidebook guidebook = createGuidebook(UUID.randomUUID(), 11);
      User user = createUser(userId);
      GuidebookReview review = createReview(reviewId, guidebook, user, 4, "기존 내용");

      when(guidebookReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
      when(guidebookReviewMapper.toResponse(review)).thenReturn(mock(GuidebookReviewResponse.class));

      // when
      guidebookReviewService.update(reviewId, userId, request);

      // then
      verify(guidebookRepository, never()).updateRating(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("존재하지 않는 리뷰이면 예외 발생")
    void failsByNotFound() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewUpdateRequest request = new GuidebookReviewUpdateRequest(5, "수정된 내용");

      when(guidebookReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(GuidebookReviewNotFoundException.class,
          () -> guidebookReviewService.update(reviewId, userId, request));
    }

    @Test
    @DisplayName("리뷰 작성자가 아니면 예외 발생")
    void failsByAuthorMismatch() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      UUID anotherUserId = UUID.randomUUID();
      GuidebookReviewUpdateRequest request = new GuidebookReviewUpdateRequest(5, "수정된 내용");

      Guidebook guidebook = createGuidebook(UUID.randomUUID(), 11);
      User author = createUser(authorId);
      GuidebookReview review = createReview(reviewId, guidebook, author, 3, "기존 내용");

      when(guidebookReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

      // when & then
      assertThrows(GuidebookReviewAuthorMismatchException.class,
          () -> guidebookReviewService.update(reviewId, anotherUserId, request));
    }
  }

  /***
   * 이하 편의 메서드
   */
  private User createUser(UUID userId) {
    User user = User.builder()
        .nickname("테스트유저")
        .password("password")
        .email("test@test.com")
        .build();
    ReflectionTestUtils.setField(user, "id", userId);
    return user;
  }

  private Guidebook createGuidebook(UUID guidebookId, int totalPlaceCount) {
    Guidebook guidebook = Guidebook.builder()
        .title("테스트 가이드북")
        .totalPlaceCount(totalPlaceCount)
        .author(createUser(UUID.randomUUID()))
        .build();
    ReflectionTestUtils.setField(guidebook, "id", guidebookId);
    return guidebook;
  }

  private GuidebookParticipation createParticipation(Guidebook guidebook, User user,
      int completedPlaceCount) {
    return GuidebookParticipation.builder()
        .guidebook(guidebook)
        .user(user)
        .completedPlaceCount(completedPlaceCount)
        .build();
  }

  private GuidebookReview createReview(UUID reviewId, Guidebook guidebook, User author,
      int rating, String content) {
    GuidebookReview review = GuidebookReview.builder()
        .guidebook(guidebook)
        .author(author)
        .rating(rating)
        .content(content)
        .build();
    ReflectionTestUtils.setField(review, "id", reviewId);
    return review;
  }
}