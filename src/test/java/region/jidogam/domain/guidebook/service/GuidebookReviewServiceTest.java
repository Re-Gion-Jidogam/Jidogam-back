package region.jidogam.domain.guidebook.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import region.jidogam.domain.guidebook.dto.GuidebookReviewCreateRequest;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookParticipation;
import region.jidogam.domain.guidebook.entity.GuidebookReview;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.exception.GuidebookNotParticipatedException;
import region.jidogam.domain.guidebook.exception.GuidebookNotPublishedException;
import region.jidogam.domain.guidebook.exception.GuidebookReviewDuplicateException;
import region.jidogam.domain.guidebook.exception.GuidebookReviewInsufficientVisitsException;
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

      Guidebook guidebook = mock(Guidebook.class);
      User user = mock(User.class);
      GuidebookParticipation participation = mock(GuidebookParticipation.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(guidebook.getIsPublished()).thenReturn(true);
      when(guidebook.getTotalPlaceCount()).thenReturn(11);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.of(participation));
      when(participation.getCompletedPlaceCount()).thenReturn(5);

      when(guidebookReviewRepository.existsByGuidebook_IdAndAuthor_Id(guidebookId, userId))
          .thenReturn(false);

      // when
      guidebookReviewService.create(guidebookId, userId, request);

      // then
      verify(guidebookReviewRepository).save(any(GuidebookReview.class));
      verify(guidebook).addRating(request.rating());
    }

    @Test
    @DisplayName("소규모 가이드북(장소 10개 이하)에서 4개 이상 방문 시 리뷰 저장 성공")
    void successSmallGuidebook() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(5, "소규모 가이드북 리뷰입니다.");

      Guidebook guidebook = mock(Guidebook.class);
      User user = mock(User.class);
      GuidebookParticipation participation = mock(GuidebookParticipation.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(guidebook.getIsPublished()).thenReturn(true);
      when(guidebook.getTotalPlaceCount()).thenReturn(10);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.of(participation));
      when(participation.getCompletedPlaceCount()).thenReturn(4);

      when(guidebookReviewRepository.existsByGuidebook_IdAndAuthor_Id(guidebookId, userId))
          .thenReturn(false);

      // when
      guidebookReviewService.create(guidebookId, userId, request);

      // then
      verify(guidebookReviewRepository).save(any(GuidebookReview.class));
      verify(guidebook).addRating(request.rating());
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
    @DisplayName("출판되지 않은 가이드북이면 예외 발생")
    void failsByNotPublished() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(4, "내용");

      Guidebook guidebook = mock(Guidebook.class);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(guidebook.getIsPublished()).thenReturn(false);

      // when & then
      assertThrows(GuidebookNotPublishedException.class,
          () -> guidebookReviewService.create(guidebookId, userId, request));
    }

    @Test
    @DisplayName("존재하지 않는 사용자이면 예외 발생")
    void failsByUserNotFound() {
      // given
      UUID guidebookId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      GuidebookReviewCreateRequest request = new GuidebookReviewCreateRequest(4, "내용");

      Guidebook guidebook = mock(Guidebook.class);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(guidebook.getIsPublished()).thenReturn(true);
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

      Guidebook guidebook = mock(Guidebook.class);
      User user = mock(User.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(guidebook.getIsPublished()).thenReturn(true);
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

      Guidebook guidebook = mock(Guidebook.class);
      User user = mock(User.class);
      GuidebookParticipation participation = mock(GuidebookParticipation.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(guidebook.getIsPublished()).thenReturn(true);
      when(guidebook.getTotalPlaceCount()).thenReturn(totalPlaceCount);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.of(participation));
      when(participation.getCompletedPlaceCount()).thenReturn(completedPlaceCount);

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

      Guidebook guidebook = mock(Guidebook.class);
      User user = mock(User.class);
      GuidebookParticipation participation = mock(GuidebookParticipation.class);

      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));
      when(guidebook.getIsPublished()).thenReturn(true);
      when(guidebook.getTotalPlaceCount()).thenReturn(11);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      when(guidebookParticipationRepository.findByGuidebookAndUser(guidebook, user))
          .thenReturn(Optional.of(participation));
      when(participation.getCompletedPlaceCount()).thenReturn(5);

      when(guidebookReviewRepository.existsByGuidebook_IdAndAuthor_Id(guidebookId, userId))
          .thenReturn(true);

      // when & then
      assertThrows(GuidebookReviewDuplicateException.class,
          () -> guidebookReviewService.create(guidebookId, userId, request));

      verify(guidebookReviewRepository, never()).save(any());
    }
  }
}