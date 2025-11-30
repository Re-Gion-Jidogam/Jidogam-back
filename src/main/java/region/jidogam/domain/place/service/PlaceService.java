package region.jidogam.domain.place.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.common.dto.CoordinateRange;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.common.util.CursorCodecUtil;
import region.jidogam.common.util.DistanceCalculatorUtil;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.service.AreaService;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
import region.jidogam.domain.place.dto.PlaceCursor;
import region.jidogam.domain.place.dto.PlaceFilter;
import region.jidogam.domain.place.dto.PlaceNearByRequest;
import region.jidogam.domain.place.dto.PlacePopularRequest;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.dto.PlaceSortBy;
import region.jidogam.domain.place.dto.PlaceVisitInfo;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.exception.PlaceNotFoundException;
import region.jidogam.domain.place.mapper.PlaceMapper;
import region.jidogam.domain.place.repository.PlaceRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {

  private static final double DEFAULT_SEARCH_RADIUS_KM = 1.0;
  private static final double LOCATION_TOLERANCE_DEGREES = 0.0001;

  private final PlaceRepository placeRepository;
  private final AreaService areaService;
  private final PlaceMapper placeMapper;
  private final CursorCodecUtil cursorCodecUtil;

  @Transactional(readOnly = true)
  public List<PlaceResponse> popularList(PlacePopularRequest request) {

    List<Place> topNPlaces = placeRepository.findAllByOrderByStampCountDesc(
        PageRequest.of(0, request.limit()));

    return topNPlaces.stream()
        .map(place -> placeMapper.toResponse(place, request.lat(), request.lon()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PlaceResponse> nearbyList(PlaceNearByRequest request, UUID userId) {

    double userLat = request.lat();
    double userLon = request.lon();

    CoordinateRange coordinateRange = DistanceCalculatorUtil.getCoordinateRange(
        userLat,
        userLon,
        DEFAULT_SEARCH_RADIUS_KM
    );

    List<Place> nearbyPlaces = placeRepository.findNearbyPlaces(
        userLat,
        userLon,
        coordinateRange.latMin(),
        coordinateRange.latMax(),
        coordinateRange.lonMin(),
        coordinateRange.lonMax(),
        DEFAULT_SEARCH_RADIUS_KM,
        PageRequest.of(0, request.limit())
    );

    Map<UUID, LocalDateTime> visitedDateMap = getVisitedDateMap(userId, nearbyPlaces);

    return nearbyPlaces.stream()
        .map(place -> placeMapper.toResponse(
            place,
            request.lat(),
            request.lon(),
            visitedDateMap.get(place.getId())))
        .toList();
  }

  /**
   * 가이드북에 포함된 장소 목록을 거리 순으로 조회합니다. (내부 서비스용)
   * <p>
   * 약 10m 정도의 오차는 허용하며 그 이상의 위치가 달라지는 경우 첫페이지를 응답합니다.
   */
  @Transactional(readOnly = true)
  public CursorPageResponseDto<PlaceResponse> guidebookPlaceList(
      UUID guidebookId, UUID userId, double userLat, double userLon, PlaceFilter filter,
      String cursor, int size
  ) {

    PlaceCursor placeCursor = cursorCodecUtil.decodeplaceCursor(cursor, PlaceSortBy.DISTANCE);

    if (placeCursor != null) {
      if (Math.abs(placeCursor.userLat() - userLat) > LOCATION_TOLERANCE_DEGREES ||
          Math.abs(placeCursor.userLon() - userLon) > LOCATION_TOLERANCE_DEGREES) {
        placeCursor = null;
      }
    }

    List<Place> byGuidebookOrderByDistance = placeRepository.findPlacesByGuidebookOrderByDistance(
        userLat,
        userLon,
        userId,
        guidebookId,
        filter.getValue(),
        placeCursor != null ? placeCursor.distance() : null,
        placeCursor != null ? placeCursor.lastId() : null,
        size + 1
    );

    boolean hasNext = byGuidebookOrderByDistance.size() > size;
    if (hasNext) {
      byGuidebookOrderByDistance.remove(size);
    }

    Map<UUID, LocalDateTime> visitedDateMap = getVisitedDateMap(userId, byGuidebookOrderByDistance);

    List<PlaceResponse> responses = byGuidebookOrderByDistance.stream()
        .map(place -> placeMapper.toResponse(
            place,
            userLat,
            userLon,
            visitedDateMap.get(place.getId())))
        .toList();

    String nextCursor = null;
    if (hasNext) {
      nextCursor = cursorCodecUtil.encodeNextCursor(
          responses.get(responses.size() - 1),
          PlaceSortBy.DISTANCE,
          userLat,
          userLon
      );
    }

    return CursorPageResponseDto.<PlaceResponse>builder()
        .data(responses)
        .size(responses.size())
        .hasNext(hasNext)
        .nextCursor(nextCursor)
        .sortBy(PlaceSortBy.DISTANCE.getValue())
        .sortDirection(SortDirection.DESC)
        .build();
  }

  @Transactional
  public Place getOrCreatePlace(UUID id, PlaceCreateRequest request) {

    // TODO: id가 없을때, placeId로도 조회하여 확인하는 로직 필요

    if (id != null) {
      return placeRepository.findById(id)
          .orElseThrow(() -> PlaceNotFoundException.withId(id));
    }
    return createPlace(request);
  }

  // 내부 서비스용
  @Transactional
  public Place createPlace(PlaceCreateRequest request) {
    log.info("장소 생성 시작: placeName = {}", request.placeName());

    // 1. area 정보 조회
    Area area = areaService.getAreaByAddress(request.addressName());

    // 2. 장소 포인트 설정
    int points = calculatePoint(area.getWeight());

    // 3. 장소 생성
    Place place = Place.builder()
        .name(request.placeName())
        .address(request.addressName())
        .x(request.x())
        .y(request.y())
        .category(request.category())
        .area(area)
        .points(points)
        .build();

    log.info("장소 생성 완료: placeName = {}", request.placeName());
    return placeRepository.save(place);
  }

  private int calculatePoint(Integer weight) {
    return weight * 10; // 임시
  }

  private Map<UUID, LocalDateTime> getVisitedDateMap(UUID userId, List<Place> places) {
    if (userId == null || places.isEmpty()) {
      return Collections.emptyMap();
    }

    List<UUID> placeIds = places.stream()
        .map(Place::getId)
        .toList();

    return placeRepository.findVisitedDatesByUserAndPlaces(userId, placeIds)
        .stream()
        .collect(Collectors.toMap(
            PlaceVisitInfo::placeId,
            PlaceVisitInfo::visitedDate
        ));
  }
}
