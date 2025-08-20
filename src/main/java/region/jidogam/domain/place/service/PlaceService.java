package region.jidogam.domain.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.service.AreaService;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.repository.PlaceRepository;

@Service
@RequiredArgsConstructor
public class PlaceService {

  private final PlaceRepository placeRepository;
  private final AreaService areaService;

  // 내부 서비스용
  @Transactional
  public Place createPlace(PlaceCreateRequest request) {

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

    return placeRepository.save(place);
  }

  private int calculatePoint(Integer weight) {
    return weight * 10; // 임시
  }
}
