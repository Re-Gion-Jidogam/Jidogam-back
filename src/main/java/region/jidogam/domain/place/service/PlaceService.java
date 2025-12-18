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
import region.jidogam.common.util.DistanceCalculatorUtil;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.service.AreaService;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
import region.jidogam.domain.place.dto.PlaceNearByRequest;
import region.jidogam.domain.place.dto.PlacePopularRequest;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.dto.PlaceVisitInfo;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.exception.PlaceMismatchException;
import region.jidogam.domain.place.exception.PlaceNotFoundException;
import region.jidogam.domain.place.mapper.PlaceMapper;
import region.jidogam.domain.place.repository.PlaceRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {

  private final double DEFAULT_SEARCH_RADIUS_KM = 1.0;

  private final PlaceRepository placeRepository;
  private final AreaService areaService;
  private final PlaceUpdateService changeTrackingService;
  private final PointService pointService;
  private final PlaceMapper placeMapper;

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

  // 내부 서비스용
  @Transactional
  public Place getOrCreatePlace(UUID id, PlaceCreateRequest request) {

    if (id != null) {
      Place place = placeRepository.findById(id)
          .orElseThrow(() -> PlaceNotFoundException.withId(id));

      if (!place.getKakaoId().equals(request.id())) {
        throw PlaceMismatchException.idMismatch(id, request.id());
      }

      changeTrackingService.detectUpdateAndRecord(place, request);
      return place;
    }

    return placeRepository.findByKakaoId(request.id())
        .map(place -> {
          changeTrackingService.detectUpdateAndRecord(place, request);
          return place;
        })
        .orElseGet(() -> createPlace(request));
  }

  // 내부 서비스용
  @Transactional
  public Place createPlace(PlaceCreateRequest request) {
    log.info("장소 생성 시작: placeName = {}", request.placeName());

    // 1. area 정보 조회
    Area area = areaService.getAreaByAddress(request.addressName());

    // 2. 장소 포인트 설정
    int points = pointService.calculatePlacePoint(area.getWeight());

    // 3. 장소 생성
    Place place = Place.builder()
        .kakaoId(request.id())
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
