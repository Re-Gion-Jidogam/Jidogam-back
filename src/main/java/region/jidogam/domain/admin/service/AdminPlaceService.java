package region.jidogam.domain.admin.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.admin.dto.AdminPlaceCreateRequest;
import region.jidogam.domain.admin.dto.AdminPlaceResponse;
import region.jidogam.domain.admin.dto.AdminPlaceSearchRequest;
import region.jidogam.domain.admin.dto.FieldChange;
import region.jidogam.domain.admin.entity.AdminActionHistory.ActionType;
import region.jidogam.domain.admin.entity.AdminActionHistory.TargetType;
import region.jidogam.domain.admin.event.AdminActionEvent;
import region.jidogam.domain.admin.repository.AdminPlaceRepository;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.entity.PlaceChangeHistory;
import region.jidogam.domain.place.entity.PlaceChangeHistory.ChangeSource;
import region.jidogam.domain.place.exception.PlaceDuplicateException;
import region.jidogam.domain.place.exception.PlaceNotFoundException;
import region.jidogam.domain.place.repository.PlaceChangeHistoryRepository;
import region.jidogam.domain.place.repository.PlaceRepository;
import region.jidogam.domain.place.service.PlaceService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPlaceService {

  private final PlaceRepository placeRepository;
  private final AdminPlaceRepository adminPlaceRepository;
  private final PlaceChangeHistoryRepository placeChangeHistoryRepository;
  private final PlaceService placeService;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public Page<AdminPlaceResponse> getPlaces(AdminPlaceSearchRequest request) {
    PageRequest pageable = PageRequest.of(request.page(), request.size());

    return adminPlaceRepository.searchPlaces(
        request.keyword(), request.deleted(), pageable
    ).map(AdminPlaceResponse::from);
  }

  @Transactional(readOnly = true)
  public AdminPlaceResponse getPlace(UUID placeId) {
    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> PlaceNotFoundException.withId(placeId));

    return AdminPlaceResponse.from(place);
  }

  @Transactional
  public AdminPlaceResponse createPlace(AdminPlaceCreateRequest request, UUID currentAdminId) {
    validateCreateRequest(request);

    if (placeRepository.findByKakaoId(request.kakaoId()).isPresent()) {
      throw PlaceDuplicateException.withKakaoId(request.kakaoId());
    }

    Place place = placeService.createPlace(new PlaceCreateRequest(
        request.kakaoId(),
        request.name(),
        request.address(),
        request.category(),
        request.y(),
        request.x()
    ));

    log.info("관리자에 의해 장소 생성: placeId = {}, adminId = {}", place.getId(), currentAdminId);

    eventPublisher.publishEvent(AdminActionEvent.of(
        currentAdminId, ActionType.CREATE, TargetType.PLACE, place.getId()
    ));

    return AdminPlaceResponse.from(place);
  }

  @Transactional
  public AdminPlaceResponse updatePlaceExp(UUID placeId, Integer exp, UUID currentAdminId) {
    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> PlaceNotFoundException.withId(placeId));

    if (exp == null || exp < 0) {
      throw new IllegalArgumentException("포인트는 0 이상의 값이어야 합니다.");
    }

    if (exp.equals(place.getExp())) {
      return AdminPlaceResponse.from(place);
    }

    Integer oldExp = place.getExp();
    place.updateExp(exp);

    log.info("관리자에 의해 장소 포인트 수정: placeId = {}, adminId = {}, exp = {} -> {}",
        placeId, currentAdminId, oldExp, exp);

    recordPlaceChangeHistory(place, oldExp, exp);

    eventPublisher.publishEvent(AdminActionEvent.of(
        currentAdminId, ActionType.UPDATE, TargetType.PLACE, placeId,
        Map.of("exp", FieldChange.of(oldExp, exp))
    ));

    return AdminPlaceResponse.from(place);
  }

  @Transactional
  public void deletePlace(UUID placeId, UUID currentAdminId) {
    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> PlaceNotFoundException.withId(placeId));

    if (place.isDeleted()) {
      log.warn("이미 삭제된 장소입니다: placeId = {}", placeId);
      return;
    }

    place.softDelete();
    log.info("관리자에 의해 장소 삭제: placeId = {}, adminId = {}", placeId, currentAdminId);

    eventPublisher.publishEvent(AdminActionEvent.of(
        currentAdminId, ActionType.DELETE, TargetType.PLACE, placeId
    ));
  }

  @Transactional
  public void restorePlace(UUID placeId, UUID currentAdminId) {
    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> PlaceNotFoundException.withId(placeId));

    if (!place.isDeleted()) {
      log.warn("삭제되지 않은 장소입니다: placeId = {}", placeId);
      return;
    }

    place.restore();
    log.info("관리자에 의해 장소 복구: placeId = {}, adminId = {}", placeId, currentAdminId);

    eventPublisher.publishEvent(AdminActionEvent.of(
        currentAdminId, ActionType.RESTORE, TargetType.PLACE, placeId
    ));
  }

  private void validateCreateRequest(AdminPlaceCreateRequest request) {
    if (request.kakaoId() == null || request.kakaoId().isBlank()) {
      throw new IllegalArgumentException("카카오 장소 ID는 필수입니다.");
    }
    if (request.name() == null || request.name().isBlank()) {
      throw new IllegalArgumentException("장소명은 필수입니다.");
    }
    if (request.address() == null || request.address().isBlank()) {
      throw new IllegalArgumentException("주소는 필수입니다.");
    }
    validateCoordinate(request.y(), BigDecimal.valueOf(-90), BigDecimal.valueOf(90), "위도");
    validateCoordinate(request.x(), BigDecimal.valueOf(-180), BigDecimal.valueOf(180), "경도");
  }

  private void validateCoordinate(BigDecimal value, BigDecimal min, BigDecimal max, String label) {
    if (value == null || value.compareTo(min) < 0 || value.compareTo(max) > 0) {
      throw new IllegalArgumentException("유효한 " + label + " 값이 아닙니다.");
    }
  }

  private void recordPlaceChangeHistory(Place place, Integer oldExp, Integer newExp) {
    placeChangeHistoryRepository.save(PlaceChangeHistory.builder()
        .placeId(place.getId())
        .kakaoId(place.getKakaoId())
        .changedFields(Map.of("exp", region.jidogam.domain.place.dto.FieldChange.of(
            oldExp.toString(), newExp.toString())))
        .source(ChangeSource.ADMIN)
        .build());
  }
}
