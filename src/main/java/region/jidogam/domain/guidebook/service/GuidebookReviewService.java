package region.jidogam.domain.guidebook.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.guidebook.dto.GuidebookReviewCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookReviewResponse;
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

@Service
@RequiredArgsConstructor
public class GuidebookReviewService {

  private static final int MIN_VISITED_PLACE_COUNT_SMALL = 4;
  private static final int MIN_VISITED_PLACE_COUNT_DEFAULT = 5;
  private static final int SMALL_GUIDEBOOK_PLACE_THRESHOLD = 10;

  private final GuidebookRepository guidebookRepository;
  private final GuidebookReviewRepository guidebookReviewRepository;
  private final GuidebookParticipationRepository guidebookParticipationRepository;
  private final UserRepository userRepository;
  private final GuidebookReviewMapper guidebookReviewMapper;

  @Transactional
  public GuidebookReviewResponse create(UUID guidebookId, UUID userId,
      GuidebookReviewCreateRequest request) {

    Guidebook guidebook = getGuidebookOrThrow(guidebookId);
    User user = getUserOrThrow(userId);

    GuidebookParticipation participation = guidebookParticipationRepository
        .findByGuidebookAndUser(guidebook, user)
        .orElseThrow(() -> GuidebookNotParticipatedException.withId(guidebookId));

    int minRequired = guidebook.getTotalPlaceCount() <= SMALL_GUIDEBOOK_PLACE_THRESHOLD
        ? MIN_VISITED_PLACE_COUNT_SMALL
        : MIN_VISITED_PLACE_COUNT_DEFAULT;

    if (participation.getCompletedPlaceCount() < minRequired) {
      throw new GuidebookReviewInsufficientVisitsException(
          minRequired, participation.getCompletedPlaceCount());
    }

    guidebookReviewRepository.findByGuidebook_IdAndAuthor_Id(guidebookId, userId)
        .ifPresent(existing -> {
          if (existing.getDeletedAt() != null) {
            throw GuidebookReviewDeletedDuplicateException.withId(guidebookId);
          }
          throw GuidebookReviewDuplicateException.withId(guidebookId);
        });

    GuidebookReview review = GuidebookReview.builder()
        .guidebook(guidebook)
        .author(user)
        .content(request.content())
        .rating(request.rating())
        .build();

    GuidebookReview savedReview = guidebookReviewRepository.save(review);
    guidebookRepository.updateRating(guidebook.getId(), request.rating(), 1);
    return guidebookReviewMapper.toResponse(savedReview);
  }

  @Transactional
  public GuidebookReviewResponse update(UUID reviewId, UUID userId,
      GuidebookReviewUpdateRequest request) {

    GuidebookReview review = getOrThrow(reviewId);

    if (!review.getAuthor().getId().equals(userId)) {
      throw GuidebookReviewAuthorMismatchException.withId(reviewId);
    }

    Optional.ofNullable(request.rating())
        .ifPresent(newRating -> {
          if (!newRating.equals(review.getRating())) {
            guidebookRepository.updateRating(review.getGuidebook().getId(),
                newRating - review.getRating(), 0);
            review.updateRating(newRating);
          }
        });

    Optional.ofNullable(request.content())
        .ifPresent(review::updateContent);

    return guidebookReviewMapper.toResponse(review);
  }

  @Transactional
  public void delete(UUID reviewId, UUID userId) {

    GuidebookReview review = getOrThrow(reviewId);

    if (!review.getAuthor().getId().equals(userId)) {
      throw GuidebookReviewAuthorMismatchException.withId(reviewId);
    }

    guidebookRepository.updateRating(review.getGuidebook().getId(), -review.getRating(), -1);
    review.softDelete();
  }

  private Guidebook getGuidebookOrThrow(UUID guidebookId) {
    return guidebookRepository.findById(guidebookId)
        .orElseThrow(() -> GuidebookNotFoundException.withId(guidebookId));
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
  }


  private GuidebookReview getOrThrow(UUID id) {
    return guidebookReviewRepository.findById(id)
        .orElseThrow(() -> GuidebookReviewNotFoundException.withId(id));
  }

}