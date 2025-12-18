package region.jidogam.domain.place.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.service.AreaService;
import region.jidogam.domain.place.dto.FieldChange;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.entity.PlaceChangeHistory;
import region.jidogam.domain.place.entity.PlaceChangeHistory.ChangeSource;
import region.jidogam.domain.place.repository.PlaceChangeHistoryRepository;

/**
 * Place 변경사항을 감지하고, 업데이트하고, 이력을 기록
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceUpdateService {

  private final PlaceChangeHistoryRepository historyRepository;
  private final AreaService areaService;
  private final PointService pointService;
  private final ObjectMapper objectMapper;

  /**
   * Place를 업데이트하고 변경 이력을 기록합니다.
   *
   * @param place   업데이트할 장소
   * @param request 프론트에서 카카오 API 호출 후 전달한 최신 장소 정보
   */
  @Transactional
  public void detectUpdateAndRecord(Place place, PlaceCreateRequest request) {
    Map<String, FieldChange> changes = new HashMap<>();

    // 이름 변경
    if (!Objects.equals(place.getName(), request.placeName())) {
      changes.put("name", FieldChange.of(place.getName(), request.placeName()));
      place.updateName(request.placeName());
    }

    // 카테고리 변경
    if (!Objects.equals(place.getCategory(), request.category())) {
      changes.put("category", FieldChange.of(place.getCategory(), request.category()));
      place.updateCategory(request.category());
    }

    // 주소 변경
    if (!Objects.equals(place.getAddress(), request.addressName())) {
      updateAddressAndRelatedFields(place, request, changes);
    }

    // 변경사항이 있으면 이력 저장
    if (!changes.isEmpty()) {
      recordChangeHistory(place.getId(), place.getKakaoId(), changes);
      log.info("Place updated: placeId={}, changedFields={}", place.getId(), changes.keySet());
    }
  }

  /**
   * 주소 변경에 따른 연쇄 업데이트 (좌표, 지역, 포인트)
   * <p>
   * 주소가 변경되면 다음 항목들이 함께 변경됩니다:
   * - 좌표 (x, y)
   * - 지역 (Area)
   * - 포인트 (지역 가중치 기반)
   */
  private void updateAddressAndRelatedFields(Place place, PlaceCreateRequest request,
      Map<String, FieldChange> changes) {
    // 변경 전 값 저장
    String oldCoordinates = place.getX() + "," + place.getY();
    String oldAreaId = place.getArea() != null ? place.getArea().getId().toString() : null;
    Integer oldPoint = place.getPoints();

    // 새로운 값 계산
    String newCoordinates = request.x() + "," + request.y();
    Area newArea = areaService.getAreaByAddress(request.addressName());
    int newPoint = pointService.calculatePlacePoint(newArea.getWeight());

    // 변경 기록
    changes.put("address", FieldChange.of(place.getAddress(), request.addressName()));
    changes.put("coordinates", FieldChange.of(oldCoordinates, newCoordinates));
    changes.put("areaId", FieldChange.of(oldAreaId, newArea.getId().toString()));
    changes.put("points", FieldChange.of(oldPoint.toString(), String.valueOf(newPoint)));

    // 업데이트
    place.updateAddress(request.addressName());
    place.updateCoordinates(request.x(), request.y());
    place.updateArea(newArea);
    place.updatePoint(newPoint);
  }

  private void recordChangeHistory(UUID placeId, String kakaoId, Map<String, FieldChange> changes) {
    PlaceChangeHistory history = PlaceChangeHistory.builder()
        .placeId(placeId)
        .kakaoId(kakaoId)
        .changedFields(changes)
        .source(ChangeSource.KAKAO_API)
        .build();

    historyRepository.save(history);
  }
}
