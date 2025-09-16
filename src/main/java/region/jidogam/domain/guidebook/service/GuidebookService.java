package region.jidogam.domain.guidebook.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.exception.PlaceNotFoundException;
import region.jidogam.domain.place.repository.PlaceRepository;
import region.jidogam.domain.place.service.PlaceService;
import region.jidogam.domain.stamp.repository.StampRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuidebookService {

  private final UserRepository userRepository;
  private final GuidebookRepository guidebookRepository;
  private final GuidebookPlaceRepository guidebookPlaceRepository;
  private final GuidebookParticipantRepository guidebookParticipantRepository;
  private final StampRepository stampRepository;
  private final PlaceRepository placeRepository;
  private final PlaceService placeService;
  private final GuidebookMapper guidebookMapper;

  @Transactional
  public void create(GuidebookCreateRequest request, UUID userId) {

    User user = getUserOrThrow(userId);

    if (request.thumbnail() == null) {
      if (request.color() == null || request.emoji() == null) {
        throw GuidebookBackgroundRequiredException.required();
      }
    }

    Guidebook guidebook = Guidebook.builder()
      .author(user)
      .title(request.title())
      .description(request.description())
      .emoji(request.emoji())
      .color(request.color())
      .thumbnailUrl(request.thumbnail())
      .build();

    guidebookRepository.save(guidebook);
  }

  @Transactional(readOnly = true)
  public GuidebookResponse getById(UUID id, UUID userId) {

    Guidebook guidebook = getOrThrow(id);

    int visitedPlaceCount = getVisitedPlaceCount(guidebook.getId(), userId);

    return guidebookMapper.toResponse(guidebook, visitedPlaceCount);
  }

  @Transactional
  public GuidebookResponse update(UUID id, UUID userId, GuidebookUpdateRequest request) {

    Guidebook guidebook = getOrThrow(id);

    checkAuthorOrThrow(guidebook, userId);

    Optional.ofNullable(request.title()).ifPresent(guidebook::updateTitle);
    Optional.ofNullable(request.description()).ifPresent(guidebook::updateDescription);
    Optional.ofNullable(request.color()).ifPresent(guidebook::updateColor);
    Optional.ofNullable(request.emoji()).ifPresent(guidebook::updateEmoji);
    Optional.ofNullable(request.thumbnail()).ifPresent(guidebook::updateThumbnailUrl);
    Optional.ofNullable(request.isPublish()).ifPresent(isPublish -> {
      if (isPublish) {
        guidebook.publish();
      } else {
        if (guidebook.getParticipantCount() > 0) {
          throw GuidebookUnpublishViolationException.withId(id);
        }
        guidebook.unpublish();
      }
    });

    int visitedPlaceCount = getVisitedPlaceCount(guidebook.getId(), userId);

    return guidebookMapper.toResponse(guidebook, visitedPlaceCount);
  }

  @Transactional
  public void delete(UUID id, UUID userId) {

    Guidebook guidebook = getOrThrow(id);

    checkAuthorOrThrow(guidebook, userId);

    if (guidebook.getIsPublished()) {
      throw GuidebookPublishedException.withId(id);
    }

    guidebookPlaceRepository.deleteByGuidebook(guidebook);
    guidebookRepository.delete(guidebook);
  }

  @Transactional
  public GuidebookResponse addPlace(UUID id, UUID userId, GuidebookAddPlaceRequest request) {

    Guidebook guidebook = getOrThrow(id);

    checkAuthorOrThrow(guidebook, userId);

    if (request.mapImageUrl() != null) {
      guidebook.updateMapImageUrl(request.mapImageUrl());
    }

    // 장소 추가
    Place place = placeService.getOrCreatePlace(request.pid(), request.place());
    GuidebookPlace guidebookPlace = GuidebookPlace.builder()
      .guidebook(guidebook)
      .place(place)
      .build();
    guidebookPlaceRepository.save(guidebookPlace);
    guidebook.increaseTotalPlaceCount();

    int visitedPlaceCount = getVisitedPlaceCount(guidebook.getId(), userId);
    return guidebookMapper.toResponse(guidebook, visitedPlaceCount);
  }

  @Transactional
  public void removePlace(UUID id, UUID placeId, UUID userId) {

    Guidebook guidebook = getOrThrow(id);
    Place place = getPlaceOrThrow(placeId);

    checkAuthorOrThrow(guidebook, userId);

    guidebookPlaceRepository.deleteByGuidebookAndPlace(guidebook, place);
    // TODO: TotalPlaceCount 줄이기
  }

  public void addParticipant(UUID id, UUID userId) {

    Guidebook guidebook = getOrThrow(id);
    User user = getUserOrThrow(userId);

    if (!guidebook.getIsPublished()) {
      throw GuidebookNotPublishedException.withId(guidebook.getId());
    }

    if (guidebookParticipantRepository.existsByGuidebookAndUser(guidebook, user)) {
      throw GuidebookAlreadyParticipatedException.withId(guidebook.getId());
    }

    GuidebookParticipant guidebookParticipant = GuidebookParticipant.builder()
      .guidebook(guidebook)
      .user(user)
      .build();

    guidebookParticipantRepository.save(guidebookParticipant);
    // TODO: 원자적으로 참여자 수 증가시키기
  }

  @Transactional
  public void cancelParticipation(UUID id, UUID userId) {
    if (guidebookParticipantRepository.deleteByGuidebook_IdAndUser_Id(id, userId) > 0) {
      guidebookRepository.updateParticipantCount(id, -1);
    }
  }

  private Guidebook getOrThrow(UUID id) {
    return guidebookRepository.findById(id)
      .orElseThrow(() -> GuidebookNotFoundException.withId(id));
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  private Place getPlaceOrThrow(UUID placeId) {
    return placeRepository.findById(placeId)
      .orElseThrow(() -> PlaceNotFoundException.withId(placeId));
  }

  private int getVisitedPlaceCount(UUID id, UUID userId) {
    if (userId == null) {
      return 0;
    }
    return stampRepository.countUserStampsInGuidebook(userId, id);
  }

  private void checkAuthorOrThrow(Guidebook guidebook, UUID userId) {
    if (!guidebook.getAuthor().getId().equals(userId)) {
      throw AuthorMismatchException.withId(guidebook.getId());
    }
  }
}
