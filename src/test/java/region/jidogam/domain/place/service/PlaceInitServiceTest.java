package region.jidogam.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import region.jidogam.domain.place.dto.ExternalPlaceData;
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
