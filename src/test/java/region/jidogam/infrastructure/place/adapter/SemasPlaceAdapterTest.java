package region.jidogam.infrastructure.place.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import region.jidogam.domain.place.dto.ExternalPlaceData;
import region.jidogam.infrastructure.place.dto.SemasStoreItem;
import region.jidogam.infrastructure.place.dto.SemasStoreResponse.Body;

@ExtendWith(MockitoExtension.class)
class SemasPlaceAdapterTest {

  private static final int DEFAULT_FETCH_LIMIT = 1000;

  @Mock
  private SemasPlaceClient semasPlaceClient;

  @InjectMocks
  private SemasPlaceAdapter semasPlaceAdapter;

  private SemasPlaceAdapter adapter() {
    return semasPlaceAdapter;
  }

  @Nested
  @DisplayName("시군구코드 기준 상가업소 조회")
  class GetStoresBySigunguCode {

    @Test
    @DisplayName("limit이 1000의 배수가 아니어도 페이지마다 numOfRows는 항상 1000으로 고정 요청한다")
    void requestsFixedPageSizeAcrossPages() {
      // given: 전체 2000건 중 1500건만 요청 (마지막 페이지가 부분 페이지가 되는 케이스)
      when(semasPlaceClient.fetchStoresBySigunguCode(
          eq("41210"), eq(1), eq(DEFAULT_FETCH_LIMIT), anyString(), anyString(), anyString()))
          .thenReturn(bodyOf(1, DEFAULT_FETCH_LIMIT, 2000));
      when(semasPlaceClient.fetchStoresBySigunguCode(
          eq("41210"), eq(2), eq(DEFAULT_FETCH_LIMIT), anyString(), anyString(), anyString()))
          .thenReturn(bodyOf(1001, DEFAULT_FETCH_LIMIT, 2000));

      // when
      List<ExternalPlaceData> result = adapter().getStoresBySigunguCode(
          "41210", 1500, null, null, null);

      // then: numOfRows가 줄어들지 않고 매번 1000으로 고정 요청됐는지 검증
      verify(semasPlaceClient).fetchStoresBySigunguCode(
          "41210", 1, DEFAULT_FETCH_LIMIT, "", "", "");
      verify(semasPlaceClient).fetchStoresBySigunguCode(
          "41210", 2, DEFAULT_FETCH_LIMIT, "", "", "");
    }

    @Test
    @DisplayName("응답이 limit을 초과해도 최종 결과는 limit 건수만큼만 잘라서 반환한다")
    void trimsResultToLimit() {
      // given
      when(semasPlaceClient.fetchStoresBySigunguCode(
          eq("41210"), eq(1), eq(DEFAULT_FETCH_LIMIT), anyString(), anyString(), anyString()))
          .thenReturn(bodyOf(1, DEFAULT_FETCH_LIMIT, 2000));
      when(semasPlaceClient.fetchStoresBySigunguCode(
          eq("41210"), eq(2), eq(DEFAULT_FETCH_LIMIT), anyString(), anyString(), anyString()))
          .thenReturn(bodyOf(1001, DEFAULT_FETCH_LIMIT, 2000));

      // when
      List<ExternalPlaceData> result = adapter().getStoresBySigunguCode(
          "41210", 1500, null, null, null);

      // then: 실제로 가져온 건 2000건이지만 결과는 요청한 1500건으로 잘려야 하고,
      // 앞에서부터 순서대로(1~1500번) 유지돼야 한다 (중복/누락 없이)
      assertThat(result).hasSize(1500);
      assertThat(result.get(0).externalId()).isEqualTo("1");
      assertThat(result.get(1499).externalId()).isEqualTo("1500");
    }

    @Test
    @DisplayName("limit이 한 페이지(1000건) 이하면 numOfRows도 limit만큼만 요청해서 한 번만 호출한다")
    void requestsOnlyAsManyRowsAsLimitWhenSmall() {
      // given: 불필요하게 1000건씩 받아오지 않는지 검증 (limit=50인데 numOfRows=1000이면 낭비)
      when(semasPlaceClient.fetchStoresBySigunguCode(
          eq("41210"), eq(1), eq(50), anyString(), anyString(), anyString()))
          .thenReturn(bodyOf(1, 50, 50));

      // when
      List<ExternalPlaceData> result = adapter().getStoresBySigunguCode(
          "41210", 50, null, null, null);

      // then
      assertThat(result).hasSize(50);
      verify(semasPlaceClient, times(1)).fetchStoresBySigunguCode(
          "41210", 1, 50, "", "", "");
    }
  }

  @Nested
  @DisplayName("반경 기준 상가업소 조회")
  class GetStoresByRadius {

    @Test
    @DisplayName("limit이 1000의 배수가 아니어도 페이지마다 numOfRows는 항상 1000으로 고정 요청하고, 결과는 limit만큼 잘린다")
    void requestsFixedPageSizeAndTrimsResult() {
      // given
      when(semasPlaceClient.fetchStoresByRadius(
          eq(127.0), eq(37.5), eq(1000), eq(1), eq(DEFAULT_FETCH_LIMIT),
          anyString(), anyString(), anyString()))
          .thenReturn(bodyOf(1, DEFAULT_FETCH_LIMIT, 1800));
      when(semasPlaceClient.fetchStoresByRadius(
          eq(127.0), eq(37.5), eq(1000), eq(2), eq(DEFAULT_FETCH_LIMIT),
          anyString(), anyString(), anyString()))
          .thenReturn(bodyOf(1001, 800, 1800));

      // when
      List<ExternalPlaceData> result = adapter().getStoresByRadius(
          37.5, 127.0, 1000, 1200, null, null, null);

      // then
      verify(semasPlaceClient).fetchStoresByRadius(
          127.0, 37.5, 1000, 1, DEFAULT_FETCH_LIMIT, "", "", "");
      verify(semasPlaceClient).fetchStoresByRadius(
          127.0, 37.5, 1000, 2, DEFAULT_FETCH_LIMIT, "", "", "");
      assertThat(result).hasSize(1200);
      assertThat(result.get(0).externalId()).isEqualTo("1");
      assertThat(result.get(1199).externalId()).isEqualTo("1200");
    }

    @Test
    @DisplayName("limit이 작으면 numOfRows도 limit만큼만 요청해서 한 번만 호출한다")
    void requestsOnlyAsManyRowsAsLimitWhenSmall() {
      // given: 주변 장소 동기화처럼 limit=20인 소규모 조회에서 1000건씩 받아오지 않는지 검증
      when(semasPlaceClient.fetchStoresByRadius(
          eq(127.0), eq(37.5), eq(1000), eq(1), eq(20), anyString(), anyString(), anyString()))
          .thenReturn(bodyOf(1, 20, 20));

      // when
      List<ExternalPlaceData> result = adapter().getStoresByRadius(
          37.5, 127.0, 1000, 20, null, null, null);

      // then
      assertThat(result).hasSize(20);
      verify(semasPlaceClient, times(1)).fetchStoresByRadius(
          127.0, 37.5, 1000, 1, 20, "", "", "");
    }
  }

  /**
   * startId부터 count개의 아이템을 담은 응답 바디를 만든다.
   */
  private Body bodyOf(int startId, int count, int totalCount) {
    List<SemasStoreItem> items = IntStream.range(0, count)
        .mapToObj(i -> storeItem(String.valueOf(startId + i)))
        .toList();
    return new Body(items, count, 1, totalCount);
  }

  private SemasStoreItem storeItem(String bizesId) {
    return new SemasStoreItem(
        bizesId, "상호" + bizesId, null,
        null, null, null, null, null, null,
        null, null, null, null, null, null,
        null, null, null, null, null,
        null, null, null, null, null,
        null, null, null, null, null,
        null, null, null, null, null,
        null, null);
  }
}
