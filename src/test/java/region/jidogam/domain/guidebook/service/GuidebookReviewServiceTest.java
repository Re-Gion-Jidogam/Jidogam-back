package region.jidogam.domain.guidebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
import region.jidogam.common.dto.SortDirection;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.common.util.CursorCodecUtil;
import region.jidogam.domain.guidebook.dto.GuidebookReviewConditionRequest;
import region.jidogam.domain.guidebook.dto.GuidebookReviewCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookReviewResponse;
import region.jidogam.domain.guidebook.dto.GuidebookReviewSortBy;
import region.jidogam.domain.guidebook.dto.GuidebookReviewUpdateRequest;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookParticipation;
import region.jidogam.domain.guidebook.entity.GuidebookReview;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.exception.GuidebookNotParticipatedException;
import region.jidogam.domain.guidebook.exception.GuidebookReviewAuthorMismatchException;
import region.jidogam.domain.guidebook.exception.GuidebookReviewDeletedDuplicateException;
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
  @Mock
  private CursorCodecUtil cursorCodecUtil;
  @InjectMocks
  private GuidebookReviewService guidebookReviewService;

  @Nested
  @DisplayName("리뷰 목록 조회")
  class GetReviews {

    @Test
    @DisplayName("다음 페이지 없을 때 성공")
    void success() {
      // given
      UUID guidebookId = UUID.randomUUID();
      GuidebookReviewConditionRequest request = new GuidebookReviewConditionRequest(
          GuidebookReviewSortBy.CREATED_AT, SortDirection.DESC, null, 5);

      Guidebook guidebook = createGuidebook(guidebookId, 11);
      List<GuidebookReview> reviews = List.of(
          createReview(UUID.randomUUID(), guidebook, createUser(UUID.randomUUID()), 4, "리뷰1"),
          createReview(UUID.randomUUID(), guidebook, createUser(UUID.randomUUID()), 3, "리뷰2")
      );

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(cursorCodecUtil.decodeGuidebookReviewCursor(null)).thenReturn(null);
      when(guidebookReviewRepository.searchByGuidebookId(guidebookId, null, SortDirection.DESC, 6))
          .thenReturn(new ArrayList<>(reviews));
      when(guidebookReviewRepository.countByGuidebookId(guidebookId)).thenReturn(2L);
      reviews.forEach(r -> when(guidebookReviewMapper.toResponse(r))
          .thenReturn(mock(GuidebookReviewResponse.class)));

      // when
      CursorPageResponseDto<GuidebookReviewResponse> result =
          guidebookReviewService.getReviews(guidebookId, request);

      // then
      assertThat(result.hasNext()).isFalse();
      assertThat(result.nextCursor()).isNull();
      assertThat(result.data()).hasSize(2);
      assertThat(result.totalCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("다음 페이지 있을 때 nextCursor 반환")
    void successWithHasNext() {
      // given
      UUID guidebookId = UUID.randomUUID();
      int limit = 2;
      GuidebookReviewConditionRequest request = new GuidebookReviewConditionRequest(
          GuidebookReviewSortBy.CREATED_AT, SortDirection.DESC, null, limit);

      Guidebook guidebook = createGuidebook(guidebookId, 11);
      List<GuidebookReview> reviews = new ArrayList<>(List.of(
          createReview(UUID.randomUUID(), guidebook, createUser(UUID.randomUUID()), 5, "리뷰1"),
          createReview(UUID.randomUUID(), guidebook, createUser(UUID.randomUUID()), 4, "리뷰2"),
          createReview(UUID.randomUUID(), guidebook, createUser(UUID.randomUUID()), 3, "리뷰3")
      ));

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(cursorCodecUtil.decodeGuidebookReviewCursor(null)).thenReturn(null);
      when(guidebookReviewRepository.searchByGuidebookId(guidebookId, null, SortDirection.DESC,
          limit + 1))
          .thenReturn(reviews);
      when(guidebookReviewRepository.countByGuidebookId(guidebookId)).thenReturn(10L);
      reviews.stream().limit(limit).forEach(r -> when(guidebookReviewMapper.toResponse(r))
          .thenReturn(mock(GuidebookReviewResponse.class)));
      when(cursorCodecUtil.encodeNextCursor(any(GuidebookReviewResponse.class)))
          .thenReturn("nextCursorValue");

      // when
      CursorPageResponseDto<GuidebookReviewResponse> result =
          guidebookReviewService.getReviews(guidebookId, request);

      // then
      assertThat(result.hasNext()).isTrue();
      assertThat(result.nextCursor()).isEqualTo("nextCursorValue");
      assertThat(result.data()).hasSize(limit);
    }

    @Test
    @DisplayName("존재하지 않는 가이드북이면 예외 발생")
    void failsByGuidebookNotFound() {
      // given
      UUID guidebookId = UUID.randomUUID();
      GuidebookReviewConditionRequest request = new GuidebookReviewConditionRequest(
          GuidebookReviewSortBy.CREATED_AT, SortDirection.DESC, null, 5);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(GuidebookNotFoundException.class,
          () -> guidebookReviewService.getReviews(guidebookId, request));
    }
  }

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
      when(guidebookReviewRepository.findByGuidebook_IdAndAuthor_Id(guidebookId, userId))
          .thenReturn(Optional.empty());
      when(guidebookReviewRepository.save(any(GuidebookReview.class))).thenReturn(savedReview);
      when(guidebookReviewMapper.toResponse(savedReview)).thenReturn(
          mock(GuidebookReviewResponse.class));

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
      when(guidebookReviewRepository.findByGuidebook_IdAndAuthor_Id(guidebookId, userId))
          .thenReturn(Optional.empty());
      when(guidebookReviewRepository.save(any(GuidebookReview.class))).thenReturn(savedReview);
      when(guidebookReviewMapper.toResponse(savedReview)).thenReturn(
          mock(GuidebookReviewResponse.class));

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
      GuidebookParticipation participation = createParticipation(guidebook, user,
          completedPlaceCount);

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
      GuidebookReview existingReview = createReview(UUID.randomUUID(), guidebook, user, 4, "기존 내용");

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.of(participation));
      when(guidebookReviewRepository.findByGuidebook_IdAndAuthor_Id(guidebookId, userId))
          .thenReturn(Optional.of(existingReview));

      // when & then
      assertThrows(GuidebookReviewDuplicateException.class,
          () -> guidebookReviewService.create(guidebookId, userId, request));

      verify(guidebookReviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("삭제된 리뷰가 있는 경우 별도 예외 발생")
    void failsByDeletedReview() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(4, "내용");

      Guidebook guidebook = createGuidebook(guidebookId, 11);
      User user = createUser(userId);
      GuidebookParticipation participation = createParticipation(guidebook, user, 5);
      GuidebookReview deletedReview = createReview(UUID.randomUUID(), guidebook, user, 4, "삭제된 내용");
      deletedReview.softDelete();

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.of(participation));
      when(guidebookReviewRepository.findByGuidebook_IdAndAuthor_Id(guidebookId, userId))
          .thenReturn(Optional.of(deletedReview));

      // when & then
      assertThrows(GuidebookReviewDeletedDuplicateException.class,
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
      when(guidebookReviewMapper.toResponse(review)).thenReturn(
          mock(GuidebookReviewResponse.class));

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
      when(guidebookReviewMapper.toResponse(review)).thenReturn(
          mock(GuidebookReviewResponse.class));

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
      when(guidebookReviewMapper.toResponse(review)).thenReturn(
          mock(GuidebookReviewResponse.class));

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

  @Nested
  @DisplayName("리뷰 삭제")
  class Delete {

    @Test
    @DisplayName("삭제 성공 시 guidebook ratingSum과 ratingCount 감소")
    void success() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Guidebook guidebook = createGuidebook(guidebookId, 11);
      User user = createUser(userId);
      GuidebookReview review = createReview(reviewId, guidebook, user, 4, "내용");

      when(guidebookReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

      // when
      guidebookReviewService.delete(reviewId, userId);

      // then
      verify(guidebookRepository).updateRating(guidebookId, -4, -1);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰이면 예외 발생")
    void failsByNotFound() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(guidebookReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(GuidebookReviewNotFoundException.class,
          () -> guidebookReviewService.delete(reviewId, userId));
    }

    @Test
    @DisplayName("리뷰 작성자가 아니면 예외 발생")
    void failsByAuthorMismatch() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      UUID anotherUserId = UUID.randomUUID();

      Guidebook guidebook = createGuidebook(UUID.randomUUID(), 11);
      User author = createUser(authorId);
      GuidebookReview review = createReview(reviewId, guidebook, author, 3, "내용");

      when(guidebookReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

      // when & then
      assertThrows(GuidebookReviewAuthorMismatchException.class,
          () -> guidebookReviewService.delete(reviewId, anotherUserId));
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