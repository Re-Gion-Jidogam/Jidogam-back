package region.jidogam.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.domain.admin.dto.AdminPlaceCreateRequest;
import region.jidogam.domain.admin.dto.AdminPlaceResponse;
import region.jidogam.domain.admin.dto.AdminPlaceSearchRequest;
import region.jidogam.domain.admin.entity.AdminActionHistory.ActionType;
import region.jidogam.domain.admin.entity.AdminActionHistory.TargetType;
import region.jidogam.domain.admin.event.AdminActionEvent;
import region.jidogam.domain.admin.repository.AdminPlaceRepository;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.place.dto.PlaceCreateRequest;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.entity.PlaceChangeHistory;
import region.jidogam.domain.place.entity.PlaceChangeHistory.ChangeSource;
import region.jidogam.domain.place.exception.PlaceDuplicateException;
import region.jidogam.domain.place.exception.PlaceNotFoundException;
import region.jidogam.domain.place.repository.PlaceChangeHistoryRepository;
import region.jidogam.domain.place.repository.PlaceRepository;
import region.jidogam.domain.place.service.PlaceService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPlaceService 테스트")
class AdminPlaceServiceTest {

  @InjectMocks
  private AdminPlaceService adminPlaceService;

  @Mock
  private PlaceRepository placeRepository;

  @Mock
  private AdminPlaceRepository adminPlaceRepository;

  @Mock
  private PlaceChangeHistoryRepository placeChangeHistoryRepository;

  @Mock
  private PlaceService placeService;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  private Area createArea() {
    return Area.builder()
        .sido("전북")
        .sigungu("익산시")
        .weight(1.0)
        .sigunguCode("45140")
        .build();
  }

  private Place createPlace(UUID id) {
    Place place = Place.builder()
        .area(createArea())
        .kakaoId("26338954")
        .name("임시마트")
        .address("전북 익산시 망산길 11-17")
        .category("마트")
        .x(BigDecimal.valueOf(126.99599512792346))
        .y(BigDecimal.valueOf(35.976749396987046))
        .exp(100)
        .build();
    ReflectionTestUtils.setField(place, "id", id);
    return place;
  }

  private AdminPlaceCreateRequest createRequest() {
    return new AdminPlaceCreateRequest(
        "26338954",
        "임시마트",
        "전북 익산시 망산길 11-17",
        "마트",
        BigDecimal.valueOf(126.99599512792346),
        BigDecimal.valueOf(35.976749396987046)
    );
  }

  @Nested
  @DisplayName("getPlaces")
  class GetPlaces {

    @Test
    @DisplayName("검색 조건으로 장소 목록을 조회한다")
    void returnsPagedPlaces() {
      AdminPlaceSearchRequest request = AdminPlaceSearchRequest.of(null, null, 0, 20);
      Place place = createPlace(UUID.randomUUID());
      Page<Place> placePage = new PageImpl<>(List.of(place));

      when(adminPlaceRepository.searchPlaces(any(), any(), any(Pageable.class)))
          .thenReturn(placePage);

      Page<AdminPlaceResponse> result = adminPlaceService.getPlaces(request);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).name()).isEqualTo("임시마트");
      assertThat(result.getContent().get(0).areaName()).isEqualTo("전북 익산시");
    }

    @Test
    @DisplayName("키워드로 필터링하여 조회한다")
    void filtersWithKeyword() {
      AdminPlaceSearchRequest request = AdminPlaceSearchRequest.of("마트", null, 0, 20);
      Page<Place> emptyPage = new PageImpl<>(List.of());

      when(adminPlaceRepository.searchPlaces(eq("마트"), any(), any(Pageable.class)))
          .thenReturn(emptyPage);

      Page<AdminPlaceResponse> result = adminPlaceService.getPlaces(request);

      assertThat(result.getContent()).isEmpty();
      verify(adminPlaceRepository).searchPlaces(eq("마트"), any(), any(Pageable.class));
    }
  }

  @Nested
  @DisplayName("getPlace")
  class GetPlace {

    @Test
    @DisplayName("장소 ID로 상세 정보를 조회한다")
    void returnsPlaceDetail() {
      UUID placeId = UUID.randomUUID();
      Place place = createPlace(placeId);
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      AdminPlaceResponse result = adminPlaceService.getPlace(placeId);

      assertThat(result.name()).isEqualTo("임시마트");
      assertThat(result.kakaoId()).isEqualTo("26338954");
      assertThat(result.exp()).isEqualTo(100);
    }

    @Test
    @DisplayName("존재하지 않는 장소 조회 시 예외가 발생한다")
    void throwsWhenPlaceNotFound() {
      UUID placeId = UUID.randomUUID();
      when(placeRepository.findById(placeId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminPlaceService.getPlace(placeId))
          .isInstanceOf(PlaceNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("createPlace")
  class CreatePlace {

    @Test
    @DisplayName("장소를 생성한다")
    void createsPlace() {
      UUID adminId = UUID.randomUUID();
      UUID placeId = UUID.randomUUID();
      Place place = createPlace(placeId);

      when(placeRepository.findByKakaoId("26338954")).thenReturn(Optional.empty());
      when(placeService.createPlace(any(PlaceCreateRequest.class))).thenReturn(place);

      AdminPlaceResponse result = adminPlaceService.createPlace(createRequest(), adminId);

      assertThat(result.id()).isEqualTo(placeId);
      assertThat(result.name()).isEqualTo("임시마트");

      ArgumentCaptor<PlaceCreateRequest> captor = ArgumentCaptor.forClass(PlaceCreateRequest.class);
      verify(placeService).createPlace(captor.capture());
      assertThat(captor.getValue().id()).isEqualTo("26338954");
      assertThat(captor.getValue().placeName()).isEqualTo("임시마트");
      assertThat(captor.getValue().addressName()).isEqualTo("전북 익산시 망산길 11-17");
    }

    @Test
    @DisplayName("이미 등록된 카카오 ID로 생성 시 예외가 발생한다")
    void throwsOnDuplicateKakaoId() {
      UUID adminId = UUID.randomUUID();
      Place existing = createPlace(UUID.randomUUID());
      when(placeRepository.findByKakaoId("26338954")).thenReturn(Optional.of(existing));

      assertThatThrownBy(() -> adminPlaceService.createPlace(createRequest(), adminId))
          .isInstanceOf(PlaceDuplicateException.class);

      verify(placeService, never()).createPlace(any());
    }

    @Test
    @DisplayName("장소명이 비어 있으면 예외가 발생한다")
    void throwsOnBlankName() {
      UUID adminId = UUID.randomUUID();
      AdminPlaceCreateRequest request = new AdminPlaceCreateRequest(
          "26338954", " ", "전북 익산시 망산길 11-17", "마트",
          BigDecimal.valueOf(126.9), BigDecimal.valueOf(35.9));

      assertThatThrownBy(() -> adminPlaceService.createPlace(request, adminId))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("장소명");
    }

    @Test
    @DisplayName("유효하지 않은 좌표면 예외가 발생한다")
    void throwsOnInvalidCoordinates() {
      UUID adminId = UUID.randomUUID();
      AdminPlaceCreateRequest request = new AdminPlaceCreateRequest(
          "26338954", "임시마트", "전북 익산시 망산길 11-17", "마트",
          BigDecimal.valueOf(126.9), BigDecimal.valueOf(95.0));

      assertThatThrownBy(() -> adminPlaceService.createPlace(request, adminId))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("위도");
    }

    @Test
    @DisplayName("장소 생성 시 CREATE 이벤트가 발행된다")
    void publishesCreateEvent() {
      UUID adminId = UUID.randomUUID();
      UUID placeId = UUID.randomUUID();
      Place place = createPlace(placeId);

      when(placeRepository.findByKakaoId("26338954")).thenReturn(Optional.empty());
      when(placeService.createPlace(any(PlaceCreateRequest.class))).thenReturn(place);

      adminPlaceService.createPlace(createRequest(), adminId);

      ArgumentCaptor<AdminActionEvent> captor = ArgumentCaptor.forClass(AdminActionEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());

      AdminActionEvent event = captor.getValue();
      assertThat(event.adminId()).isEqualTo(adminId);
      assertThat(event.actionType()).isEqualTo(ActionType.CREATE);
      assertThat(event.targetType()).isEqualTo(TargetType.PLACE);
      assertThat(event.targetId()).isEqualTo(placeId);
    }
  }

  @Nested
  @DisplayName("updatePlaceExp")
  class UpdatePlaceExp {

    @Test
    @DisplayName("장소 포인트를 수정한다")
    void updatesExp() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      AdminPlaceResponse result = adminPlaceService.updatePlaceExp(placeId, 200, adminId);

      assertThat(result.exp()).isEqualTo(200);
      assertThat(place.getExp()).isEqualTo(200);
    }

    @Test
    @DisplayName("포인트 수정 시 장소 변경 이력이 기록된다")
    void recordsPlaceChangeHistory() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      adminPlaceService.updatePlaceExp(placeId, 200, adminId);

      ArgumentCaptor<PlaceChangeHistory> captor =
          ArgumentCaptor.forClass(PlaceChangeHistory.class);
      verify(placeChangeHistoryRepository).save(captor.capture());

      PlaceChangeHistory history = captor.getValue();
      assertThat(history.getPlaceId()).isEqualTo(placeId);
      assertThat(history.getSource()).isEqualTo(ChangeSource.ADMIN);
      assertThat(history.getChangedFields().get("exp").oldValue()).isEqualTo("100");
      assertThat(history.getChangedFields().get("exp").newValue()).isEqualTo("200");
    }

    @Test
    @DisplayName("기존과 같은 포인트면 이력과 이벤트를 기록하지 않는다")
    void doesNothingWhenExpUnchanged() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      adminPlaceService.updatePlaceExp(placeId, 100, adminId);

      verify(placeChangeHistoryRepository, never()).save(any());
      verify(eventPublisher, never()).publishEvent(any(AdminActionEvent.class));
    }

    @Test
    @DisplayName("음수 포인트로 수정 시 예외가 발생한다")
    void throwsOnNegativeExp() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      assertThatThrownBy(() -> adminPlaceService.updatePlaceExp(placeId, -1, adminId))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("0 이상");
    }

    @Test
    @DisplayName("존재하지 않는 장소 수정 시 예외가 발생한다")
    void throwsWhenPlaceNotFound() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      when(placeRepository.findById(placeId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminPlaceService.updatePlaceExp(placeId, 200, adminId))
          .isInstanceOf(PlaceNotFoundException.class);
    }

    @Test
    @DisplayName("포인트 수정 시 UPDATE 이벤트가 발행된다")
    void publishesUpdateEvent() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      adminPlaceService.updatePlaceExp(placeId, 200, adminId);

      ArgumentCaptor<AdminActionEvent> captor = ArgumentCaptor.forClass(AdminActionEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());

      AdminActionEvent event = captor.getValue();
      assertThat(event.actionType()).isEqualTo(ActionType.UPDATE);
      assertThat(event.targetType()).isEqualTo(TargetType.PLACE);
      assertThat(event.targetId()).isEqualTo(placeId);
      assertThat(event.changedFields().get("exp").oldValue()).isEqualTo("100");
      assertThat(event.changedFields().get("exp").newValue()).isEqualTo("200");
    }
  }

  @Nested
  @DisplayName("deletePlace")
  class DeletePlace {

    @Test
    @DisplayName("장소를 소프트 삭제한다")
    void softDeletesPlace() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      adminPlaceService.deletePlace(placeId, adminId);

      assertThat(place.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("장소 삭제 시 DELETE 이벤트가 발행된다")
    void publishesDeleteEvent() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      adminPlaceService.deletePlace(placeId, adminId);

      ArgumentCaptor<AdminActionEvent> captor = ArgumentCaptor.forClass(AdminActionEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());

      assertThat(captor.getValue().actionType()).isEqualTo(ActionType.DELETE);
      assertThat(captor.getValue().targetType()).isEqualTo(TargetType.PLACE);
      assertThat(captor.getValue().targetId()).isEqualTo(placeId);
    }

    @Test
    @DisplayName("이미 삭제된 장소 재삭제 시 이벤트가 발행되지 않는다")
    void doesNotPublishWhenAlreadyDeleted() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      place.softDelete();
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      adminPlaceService.deletePlace(placeId, adminId);

      verify(eventPublisher, never()).publishEvent(any(AdminActionEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 장소 삭제 시 예외가 발생한다")
    void throwsWhenPlaceNotFound() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      when(placeRepository.findById(placeId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminPlaceService.deletePlace(placeId, adminId))
          .isInstanceOf(PlaceNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("restorePlace")
  class RestorePlace {

    @Test
    @DisplayName("삭제된 장소를 복구한다")
    void restoresDeletedPlace() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      place.softDelete();
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      adminPlaceService.restorePlace(placeId, adminId);

      assertThat(place.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("장소 복구 시 RESTORE 이벤트가 발행된다")
    void publishesRestoreEvent() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      place.softDelete();
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      adminPlaceService.restorePlace(placeId, adminId);

      ArgumentCaptor<AdminActionEvent> captor = ArgumentCaptor.forClass(AdminActionEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());

      assertThat(captor.getValue().actionType()).isEqualTo(ActionType.RESTORE);
      assertThat(captor.getValue().targetType()).isEqualTo(TargetType.PLACE);
    }

    @Test
    @DisplayName("삭제되지 않은 장소 복구 시 이벤트가 발행되지 않는다")
    void doesNotPublishWhenNotDeleted() {
      UUID placeId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Place place = createPlace(placeId);
      when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));

      adminPlaceService.restorePlace(placeId, adminId);

      verify(eventPublisher, never()).publishEvent(any(AdminActionEvent.class));
    }
  }
}
