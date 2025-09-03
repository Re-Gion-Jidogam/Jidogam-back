package region.jidogam.domain.guidebook.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.guidebook.dto.GuidebookAddPlaceRequest;
import region.jidogam.domain.guidebook.dto.GuidebookCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookPlace;
import region.jidogam.domain.guidebook.exception.AuthorMismatchException;
import region.jidogam.domain.guidebook.exception.GuidebookBackgroundRequiredException;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.mapper.GuidebookMapper;
import region.jidogam.domain.guidebook.repository.GuidebookPlaceRepository;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.domain.place.entity.Place;
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
  private final StampRepository stampRepository;
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
  public GuidebookResponse addPlace(UUID id, UUID userId, GuidebookAddPlaceRequest request) {

    Guidebook guidebook = getOrThrow(id);
    if (!guidebook.getAuthor().getId().equals(userId)) {
      throw AuthorMismatchException.withId(id);
    }

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

  private Guidebook getOrThrow(UUID id) {
    return guidebookRepository.findById(id)
      .orElseThrow(() -> GuidebookNotFoundException.withId(id));
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  private int getVisitedPlaceCount(UUID id, UUID userId) {
    if (userId == null) {
      return 0;
    }
    return stampRepository.countUserStampsInGuidebook(userId, id);
  }

}
