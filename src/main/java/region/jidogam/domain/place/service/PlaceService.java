package region.jidogam.domain.place.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.service.AreaService;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
import region.jidogam.domain.place.dto.PlacePopularRequest;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.exception.PlaceNotFoundException;
import region.jidogam.domain.place.mapper.PlaceMapper;
import region.jidogam.domain.place.repository.PlaceRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {

  private final PlaceRepository placeRepository;
  private final AreaService areaService;
  private final PlaceMapper placeMapper;

  @Transactional(readOnly = true)
  public List<PlaceResponse> popularList(PlacePopularRequest request) {

    List<Place> topNPlaces = placeRepository.findAllByOrderByStampCountDesc(
        PageRequest.of(0, request.limit()));

    return topNPlaces.stream()
        .map(place -> placeMapper.toResponse(place, request.lat(), request.lon()))
        .toList();
  }

  // 내부 서비스용
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

}
