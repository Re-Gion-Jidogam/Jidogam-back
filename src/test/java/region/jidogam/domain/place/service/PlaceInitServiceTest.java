package region.jidogam.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.domain.place.dto.ExternalPlaceData;
import region.jidogam.domain.place.dto.PlaceNearByRequest;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.dto.PlaceStoreInitRequest;
import region.jidogam.domain.place.dto.PlaceStoreInitResponse;
import region.jidogam.domain.place.dto.PlaceStoreInitResult;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.entity.Place.Source;
import region.jidogam.infrastructure.place.port.PlacePort;

@ExtendWith(MockitoExtension.class)
class PlaceInitServiceTest {

  @Mock
  private PlacePort placePort;

  @Mock
  private PlaceService placeService;

  @InjectMocks
  private PlaceInitService placeInitService;

  @Nested
  @DisplayName("상가업소 데이터 적재")
  class InitializeStoreData {

    @Test
    @DisplayName("모두 성공하면 시군구별 성공 건수가 그대로 집계된다")
    void Success() {
      // given
      ExternalPlaceData data1 = externalPlaceData("1");
      ExternalPlaceData data2 = externalPlaceData("2");

      PlaceStoreInitRequest request = new PlaceStoreInitRequest(
          List.of("41210"), 1000, null, null, null);

      when(placePort.getStoresBySigunguCode("41210", 1000, null, null, null))
          .thenReturn(List.of(data1, data2));

      // when
      PlaceStoreInitResponse response = placeInitService.initializeStoreData(request);

      // then
      assertThat(response.totalAttemptedCount()).isEqualTo(2);
      assertThat(response.totalSucceededCount()).isEqualTo(2);
      assertThat(response.totalFailedCount()).isZero();

      PlaceStoreInitResult result = response.results().get(0);
      assertThat(result.sigunguCode()).isEqualTo("41210");
      assertThat(result.attemptedCount()).isEqualTo(2);
      assertThat(result.succeededCount()).isEqualTo(2);
      assertThat(result.failedCount()).isZero();

      verify(placeService, times(2)).upsertPlace(any(ExternalPlaceData.class), eq(Source.SEMAS));
    }

    @Test
    @DisplayName("일부 실패해도 나머지는 계속 적재되고 실패 건수만 집계된다")
    void partialFailure() {
      // given
      ExternalPlaceData data1 = externalPlaceData("1");
      ExternalPlaceData data2 = externalPlaceData("2");
      ExternalPlaceData data3 = externalPlaceData("3");

      PlaceStoreInitRequest request = new PlaceStoreInitRequest(
          List.of("41210"), 1000, null, null, null);

      when(placePort.getStoresBySigunguCode("41210", 1000, null, null, null))
          .thenReturn(List.of(data1, data2, data3));
      when(placeService.upsertPlace(any(ExternalPlaceData.class), eq(Source.SEMAS)))
          .thenReturn(mock(Place.class));
      doThrow(new IllegalStateException("area not found"))
          .when(placeService).upsertPlace(data2, Source.SEMAS);

      // when
      PlaceStoreInitResponse response = placeInitService.initializeStoreData(request);

      // then
      assertThat(response.totalAttemptedCount()).isEqualTo(3);
      assertThat(response.totalSucceededCount()).isEqualTo(2);
      assertThat(response.totalFailedCount()).isEqualTo(1);

      // 실패한 아이템 이후(data3)도 적재가 계속 시도된다
      verify(placeService).upsertPlace(data1, Source.SEMAS);
      verify(placeService).upsertPlace(data2, Source.SEMAS);
      verify(placeService).upsertPlace(data3, Source.SEMAS);
    }

    @Test
    @DisplayName("시군구가 여러 개면 결과가 시군구별로 나뉘고 합계는 전체를 반영한다")
    void multipleSigungu() {
      // given
      ExternalPlaceData data1 = externalPlaceData("1");
      ExternalPlaceData data2 = externalPlaceData("2");

      PlaceStoreInitRequest request = new PlaceStoreInitRequest(
          List.of("41210", "41220"), 1000, null, null, null);

      when(placePort.getStoresBySigunguCode("41210", 1000, null, null, null))
          .thenReturn(List.of(data1));
      when(placePort.getStoresBySigunguCode("41220", 1000, null, null, null))
          .thenReturn(List.of(data2));
      when(placeService.upsertPlace(any(ExternalPlaceData.class), eq(Source.SEMAS)))
          .thenReturn(mock(Place.class));
      doThrow(new IllegalStateException("area not found"))
          .when(placeService).upsertPlace(data2, Source.SEMAS);

      // when
      PlaceStoreInitResponse response = placeInitService.initializeStoreData(request);

      // then
      assertThat(response.results()).hasSize(2);
      assertThat(response.totalAttemptedCount()).isEqualTo(2);
      assertThat(response.totalSucceededCount()).isEqualTo(1);
      assertThat(response.totalFailedCount()).isEqualTo(1);

      PlaceStoreInitResult firstResult = response.results().get(0);
      assertThat(firstResult.sigunguCode()).isEqualTo("41210");
      assertThat(firstResult.succeededCount()).isEqualTo(1);
      assertThat(firstResult.failedCount()).isZero();

      PlaceStoreInitResult secondResult = response.results().get(1);
      assertThat(secondResult.sigunguCode()).isEqualTo("41220");
      assertThat(secondResult.succeededCount()).isZero();
      assertThat(secondResult.failedCount()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("주변 장소 동기화")
  class SyncNearbyPlaces {

    @BeforeEach
    void enableExternalFetch() {
      ReflectionTestUtils.setField(placeInitService, "externalFetchEnabled", true);
    }

    @Test
    @DisplayName("조회된 장소를 모두 upsert한다")
    void success() {
      // given
      ExternalPlaceData data1 = externalPlaceData("1");
      ExternalPlaceData data2 = externalPlaceData("2");

      when(placePort.getStoresByRadius(37.5, 127.0, 1000, 20, null, null, null))
          .thenReturn(List.of(data1, data2));

      // when
      placeInitService.syncNearbyPlaces(37.5, 127.0, 20);

      // then
      verify(placeService).upsertPlace(data1, Source.SEMAS);
      verify(placeService).upsertPlace(data2, Source.SEMAS);
    }

    @Test
    @DisplayName("일부 장소 upsert가 실패해도 나머지는 계속 처리된다")
    void partialFailure() {
      // given
      ExternalPlaceData data1 = externalPlaceData("1");
      ExternalPlaceData data2 = externalPlaceData("2");

      when(placePort.getStoresByRadius(37.5, 127.0, 1000, 20, null, null, null))
          .thenReturn(List.of(data1, data2));
      when(placeService.upsertPlace(any(ExternalPlaceData.class), eq(Source.SEMAS)))
          .thenReturn(mock(Place.class));
      doThrow(new IllegalStateException("area not found"))
          .when(placeService).upsertPlace(data1, Source.SEMAS);

      // when & then
      assertThatCode(() -> placeInitService.syncNearbyPlaces(37.5, 127.0, 20))
          .doesNotThrowAnyException();

      verify(placeService).upsertPlace(data1, Source.SEMAS);
      verify(placeService).upsertPlace(data2, Source.SEMAS);
    }

    @Test
    @DisplayName("외부 API 조회 자체가 실패해도 예외를 전파하지 않는다")
    void externalFetchFailure() {
      // given
      when(placePort.getStoresByRadius(37.5, 127.0, 1000, 20, null, null, null))
          .thenThrow(new RuntimeException("API 호출 실패"));

      // when & then
      assertThatCode(() -> placeInitService.syncNearbyPlaces(37.5, 127.0, 20))
          .doesNotThrowAnyException();

      verify(placeService, never()).upsertPlace(any(), any());
    }

    @Test
    @DisplayName("설정이 꺼져 있으면 외부 API를 호출하지 않는다")
    void disabled() {
      // given
      ReflectionTestUtils.setField(placeInitService, "externalFetchEnabled", false);

      // when
      placeInitService.syncNearbyPlaces(37.5, 127.0, 20);

      // then
      verifyNoInteractions(placePort);
    }
  }

  @Nested
  @DisplayName("동기화 후 주변 장소 조회")
  class NearbyListWithSync {

    @BeforeEach
    void enableExternalFetch() {
      ReflectionTestUtils.setField(placeInitService, "externalFetchEnabled", true);
    }

    @Test
    @DisplayName("주변 장소를 동기화한 뒤 PlaceService의 조회 결과를 그대로 반환한다")
    void success() {
      // given
      UUID userId = UUID.randomUUID();
      PlaceNearByRequest request = new PlaceNearByRequest(37.5, 127.0, 20);
      ExternalPlaceData data = externalPlaceData("1");
      List<PlaceResponse> expected = List.of(mock(PlaceResponse.class));

      when(placePort.getStoresByRadius(37.5, 127.0, 1000, 20, null, null, null))
          .thenReturn(List.of(data));
      when(placeService.nearbyList(request, userId)).thenReturn(expected);

      // when
      List<PlaceResponse> result = placeInitService.nearbyListWithSync(request, userId);

      // then
      assertThat(result).isEqualTo(expected);
      verify(placeService).upsertPlace(data, Source.SEMAS);
      verify(placeService).nearbyList(request, userId);
    }
  }

  private ExternalPlaceData externalPlaceData(String externalId) {
    return ExternalPlaceData.builder()
        .externalId(externalId)
        .placeName("장소" + externalId)
        .jibunAddress("전북 익산시 망산길 11-17")
        .sigunguCode("41210")
        .fetchedAt(LocalDateTime.now())
        .build();
  }
}
