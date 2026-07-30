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
import region.jidogam.domain.exp.service.ExpService;
import region.jidogam.domain.place.dto.ExternalPlaceData;
import region.jidogam.domain.place.dto.FieldChange;
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
  private final ExpService expService;
  private final ObjectMapper objectMapper;

  /**
   * Place를 업데이트하고 변경 이력을 기록합니다.
   *
   * @param place 업데이트할 장소
   * @param data  외부 데이터 소스에서 가져온 최신 장소 정보
   */
  @Transactional
  public void detectUpdateAndRecord(Place place, ExternalPlaceData data, ChangeSource source) {
    Map<String, FieldChange> changes = new HashMap<>();

    // 이름 변경
    if (!Objects.equals(place.getName(), data.placeName())) {
      changes.put("name", FieldChange.of(place.getName(), data.placeName()));
      place.updateName(data.placeName());
    }

    // 카테고리 변경
    if (!Objects.equals(place.getCategoryCode(), data.categoryCode())
        || !Objects.equals(place.getCategoryName(), data.categoryName())) {
      changes.put("categoryCode", FieldChange.of(place.getCategoryCode(), data.categoryCode()));
      changes.put("categoryName", FieldChange.of(place.getCategoryName(), data.categoryName()));
      place.updateCategory(data.categoryCode(), data.categoryName());
    }

    // 주소 변경
    if (!Objects.equals(place.getJibunAddress(), data.jibunAddress())
        || !Objects.equals(place.getRoadAddress(), data.roadAddress())) {
      updateAddressAndRelatedFields(place, data, changes);
    }

    // 변경사항이 있으면 이력 저장
    if (!changes.isEmpty()) {
      recordChangeHistory(place.getId(), place.getExternalId(), changes, source);
      log.debug("Place updated: placeId={}, changedFields={}", place.getId(), changes.keySet());
    }

    // 데이터를 가져온 일시는 변경 이력과 무관하게 매 동기화마다 갱신
    place.updateFetchedAt(data.fetchedAt());
  }

  /**
   * 주소 변경에 따른 연쇄 업데이트 (좌표, 지역, 포인트)
   * <p>
   * 주소가 변경되면 다음 항목들이 함께 변경됩니다:
   * - 좌표 (x, y)
   * - 지역 (Area)
   * - 포인트 (지역 가중치 기반)
   */
  private void updateAddressAndRelatedFields(Place place, ExternalPlaceData data,
      Map<String, FieldChange> changes) {
    // 변경 전 값 저장
    String oldCoordinates = place.getX() + "," + place.getY();
    String oldAreaId = place.getArea() != null ? place.getArea().getId().toString() : null;
    Integer oldExp = place.getExp();

    // 새로운 값 계산
    String newCoordinates = data.x() + "," + data.y();
    Area newArea = areaService.getByCode(data.sigunguCode(), data.sigunguName());
    int newExp = expService.calculatePlaceExp(newArea.getWeight());

    // 변경 기록
    changes.put("jibunAddress",
        FieldChange.of(place.getJibunAddress(), data.jibunAddress()));
    changes.put("roadAddress",
        FieldChange.of(place.getRoadAddress(), data.roadAddress()));
    changes.put("coordinates", FieldChange.of(oldCoordinates, newCoordinates));
    changes.put("areaId", FieldChange.of(oldAreaId, newArea.getId().toString()));
    changes.put("exp", FieldChange.of(oldExp.toString(), String.valueOf(newExp)));

    // 업데이트
    place.updateAddress(data.jibunAddress(), data.roadAddress());
    place.updateCoordinates(data.x(), data.y());
    place.updateArea(newArea);
    place.updateExp(newExp);
  }

  private void recordChangeHistory(UUID placeId, String externalId,
      Map<String, FieldChange> changes, ChangeSource source) {
    PlaceChangeHistory history = PlaceChangeHistory.builder()
        .placeId(placeId)
        .kakaoId(externalId)
        .changedFields(changes)
        .source(source)
        .build();

    historyRepository.save(history);
  }
}
