package region.jidogam.domain.guidebook.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.common.exception.InvalidCursorException;
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
import region.jidogam.domain.guidebook.entity.GuidebookAreaRatio;
import region.jidogam.domain.guidebook.entity.GuidebookParticipant;
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
import region.jidogam.domain.guidebook.repository.GuidebookParticipantRepository;
import region.jidogam.domain.guidebook.repository.GuidebookPlaceRepository;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.domain.guidebook.utils.CursorCodecUtil;
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

  private static final int SMALL_GUIDEBOOK_THRESHOLD = 10;
  private static final int MEDIUM_GUIDEBOOK_THRESHOLD = 30;
  private static final double SMALL_GUIDEBOOK_LOCAL_RATIO = 70.0;
  private static final double MEDIUM_GUIDEBOOK_LOCAL_RATIO = 60.0;
  private static final double LARGE_GUIDEBOOK_LOCAL_RATIO = 50.0;

  private final UserRepository userRepository;
  private final GuidebookRepository guidebookRepository;
  private final GuidebookPlaceRepository guidebookPlaceRepository;
  private final GuidebookParticipantRepository guidebookParticipantRepository;
  private final GuidebookAreaRatioRepository guidebookAreaRatioRepository;
  private final StampRepository stampRepository;
  private final PlaceService placeService;
  private final GuidebookMapper guidebookMapper;
  private final CursorCodecUtil cursorCodecUtil;

  public CursorPageResponseDto<GuidebookResponse> popularList(GuidebookConditionRequest request) {
    request = new GuidebookConditionRequest(
        GuidebookFilter.POPULAR,
        GuidebookSortBy.PARTICIPANT_COUNT,
        SortDirection.DESC,
        request.cursor(),
        request.limit(),
        null
    );
    return list(request);
  }

  public CursorPageResponseDto<GuidebookResponse> list(GuidebookConditionRequest request) {

    // 1. 커서 디코딩, 없다면 null 반환
    GuidebookCursor cursor = cursorCodecUtil.decodeCursor(request.cursor());

    // + 커서 유효성 검증
    validateCursor(cursor, request.filter(), request.sortBy());

    int limit = request.limit();

    // 2. Repository 호출
    List<Guidebook> guidebooks = guidebookRepository.searchGuidebook(
        cursor,
        request.keyword(),
        request.sortBy(),
        request.sortDirection(),
        limit + 1
    );

    // 3. 응답 생성
    boolean hasNext = guidebooks.size() > limit;
    if (hasNext) {
      guidebooks.remove(limit);
    }

    List<GuidebookResponse> responses = guidebooks.stream()
        .map(guidebookMapper::toResponse)
        .collect(Collectors.toList());

    String nextCursor = null;
    if (hasNext) {
      nextCursor = cursorCodecUtil.encodeNextCursor(
          responses.get(responses.size() - 1),
          request.sortBy()
      );
    }

    return CursorPageResponseDto.<GuidebookResponse>builder()
        .data(responses)
        .size(responses.size())
        .hasNext(hasNext)
        .nextCursor(nextCursor)
        .sortBy(request.sortBy().getValue())
        .sortDirection(request.sortDirection())
        .build();
  }

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

    Optional.ofNullable(request.isPublish()).ifPresent(isPublish -> {
      if (isPublish) {
        publish(guidebook);
        guidebook.publish();
      } else {
        if (guidebook.getParticipantCount() > 0) {
          throw GuidebookUnpublishViolationException.withId(id);
        }
        guidebook.unpublish();
      }
    });
    Optional.ofNullable(request.title()).ifPresent(guidebook::updateTitle);
    Optional.ofNullable(request.description()).ifPresent(guidebook::updateDescription);
    Optional.ofNullable(request.color()).ifPresent(guidebook::updateColor);
    Optional.ofNullable(request.emoji()).ifPresent(guidebook::updateEmoji);
    Optional.ofNullable(request.thumbnail()).ifPresent(guidebook::updateThumbnailUrl);

    int visitedPlaceCount = getVisitedPlaceCount(guidebook.getId(), userId);

    return guidebookMapper.toResponse(guidebook, visitedPlaceCount);
  }

  @Transactional
  public void delete(UUID id, UUID userId) {

    Guidebook guidebook = getOrThrow(id);

    checkAuthorOrThrow(guidebook, userId);

    if (guidebook.getIsPublished()) {
      throw GuidebookPublishedException.forDeletion(id);
    }

    guidebookPlaceRepository.deleteByGuidebook(guidebook);
    guidebookRepository.delete(guidebook);
  }

  @Transactional
  public GuidebookResponse addPlace(UUID id, UUID userId, GuidebookAddPlaceRequest request) {

    Guidebook guidebook = getOrThrow(id);

    checkAuthorOrThrow(guidebook, userId);

    if (guidebook.getIsPublished()) {
      throw GuidebookPublishedException.forPlaceAddition(guidebook.getId());
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

  @Transactional
  public void removePlace(UUID id, UUID placeId, UUID userId) {

    Guidebook guidebook = getOrThrow(id);

    checkAuthorOrThrow(guidebook, userId);

    if (guidebookPlaceRepository.deleteByGuidebook_IdAndPlace_Id(id, placeId) > 0) {
      guidebook.decreaseTotalPlaceCount();
    }
  }

  @Transactional
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
    guidebookRepository.updateParticipantCount(id, 1);
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

  private void validateCursor(GuidebookCursor cursor, GuidebookFilter filter,
      GuidebookSortBy sortBy) {
    if (cursor == null || cursor.lastId() == null) {
      return;
    }

    if (filter == GuidebookFilter.POPULAR || sortBy == GuidebookSortBy.PARTICIPANT_COUNT) {
      if (cursor.participantCount() == null) {
        throw InvalidCursorException.withMessage("유효하지 않은 커서입니다.");
      }
    } else {
      if (cursor.createdAt() == null) {
        throw InvalidCursorException.withMessage("유효하지 않은 커서입니다.");
      }
    }
  }

  private void publish(Guidebook guidebook) {

    // 모든 가이드북 장소-지역 중 top3 지역 가져오기
    List<AreaRatioDto> top3Areas = guidebookPlaceRepository.findAreasByPlaceCountDesc(
        guidebook.getId(),
        PageRequest.of(0, 3)
    );

    // 출판 시 장소가 없는 경우 예외 처리
    if (top3Areas.isEmpty()) {
      throw GuidebookPublishConditionException.noPlace();
    }

    // 비율 계산하기
    List<AreaRatioDto> withRatios = top3Areas.stream()
        .map(area -> new AreaRatioDto(
            area.area(),
            area.placeCount(),
            calculateRatio(area.placeCount(), guidebook.getTotalPlaceCount())
        ))
        .collect(Collectors.toList());

    // 장소 개수와 1위 지역의 비율에 따라 local 가이드북 확인
    Boolean isLocalGuidebook = isLocalGuidebook(
        withRatios.get(0).ratio(),
        guidebook.getTotalPlaceCount()
    );

    GuidebookAreaRatio guidebookAreaRatio = GuidebookAreaRatio.builder()
        .guidebook(guidebook)
        .firstArea(withRatios.get(0).area())
        .firstAreaRatio(withRatios.get(0).ratio())
        .isPrimaryArea(isLocalGuidebook)
        .build();

    if (withRatios.size() > 1) {
      guidebookAreaRatio.setSecondArea(withRatios.get(1).area(), withRatios.get(1).ratio());
    }
    if (withRatios.size() > 2) {
      guidebookAreaRatio.setThirdArea(withRatios.get(2).area(), withRatios.get(2).ratio());
    }

    guidebookAreaRatioRepository.save(guidebookAreaRatio);
  }

  private double calculateRatio(long placeCount, int totalCount) {
    if (totalCount == 0) {
      return 0.0;
    }
    double ratio = (placeCount * 100.0) / totalCount;
    return Math.round(ratio * 100.0) / 100.0;
  }

  private boolean isLocalGuidebook(double topRatio, int totalCount) {
    // 10개 미만 : 70% 이상
    if (totalCount < SMALL_GUIDEBOOK_THRESHOLD) {
      return topRatio >= SMALL_GUIDEBOOK_LOCAL_RATIO;
    }
    // 10개 이상 30개 미만: 60% 이상
    if (totalCount < MEDIUM_GUIDEBOOK_THRESHOLD) {
      return topRatio >= MEDIUM_GUIDEBOOK_LOCAL_RATIO;
    }
    // 30개 이상: 50% 이상
    return topRatio >= LARGE_GUIDEBOOK_LOCAL_RATIO;
  }

}
