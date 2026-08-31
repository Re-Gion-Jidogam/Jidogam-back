package region.jidogam.infrastructure.place.adapter;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import region.jidogam.infrastructure.place.dto.SemasStoreResponse;

/**
 * 소상공인시장진흥공단 상가업소정보 API 호출 클라이언트.
 * <p>
 * API 요청/응답만 담당하며, 도메인 변환은 이 클라이언트를 사용하는 어댑터에서 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemasPlaceClient {

  private static final int RETRY_LIMIT = 3;
  private static final String SUCCESS_CODE = "00";
  private static final String STORE_LIST_IN_DONG_URL =
      "https://apis.data.go.kr/B553077/api/open/sdsc2/storeListInDong";
  private static final String STORE_LIST_IN_RADIUS_URL =
      "https://apis.data.go.kr/B553077/api/open/sdsc2/storeListInRadius";
  private static final String DIV_ID_SIGUNGU_CODE = "signguCd";
  private static final String RESPONSE_TYPE_JSON = "json";

  private final RestClient restClient;

  @Value("${api.place.api-key}")
  private String apiKey;

  /**
   * 시군구코드 기준으로 상가업소 목록을 조회한다.
   *
   * @param code       시군구코드
   * @param pageNo     페이지 번호
   * @param numOfRows  페이지당 조회 건수
   * @param indsLclsCd 상권업종대분류코드 (필터링하지 않으려면 빈 문자열)
   * @param indsMclsCd 상권업종중분류코드 (필터링하지 않으려면 빈 문자열)
   * @param indsSclsCd 상권업종소분류코드 (필터링하지 않으려면 빈 문자열)
   */
  public SemasStoreResponse.Body fetchStoresBySigunguCode(
      String code, int pageNo, int numOfRows, String indsLclsCd, String indsMclsCd,
      String indsSclsCd
  ) {
    log.debug("상가업소 조회 요청: code={}, pageNo={}, numOfRows={}",
        code, pageNo, numOfRows);

    SemasStoreResponse response = callApiWithRetry(() ->
        restClient.get()
            .uri(STORE_LIST_IN_DONG_URL
                    + "?ServiceKey={serviceKey}&pageNo={pageNo}&numOfRows={numOfRows}"
                    + "&divId={divId}&key={key}"
                    + "&indsLclsCd={indsLclsCd}&indsMclsCd={indsMclsCd}&indsSclsCd={indssclsCd}"
                    + "&type={type}",
                apiKey, pageNo, numOfRows, DIV_ID_SIGUNGU_CODE, code,
                indsLclsCd, indsMclsCd, indsSclsCd, RESPONSE_TYPE_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(SemasStoreResponse.class)
    );

    return response.body();
  }

  /**
   * 좌표 기준 반경 내 상가업소 목록을 조회한다.
   *
   * @param cx         중심 경도
   * @param cy         중심 위도
   * @param radius     검색 반경 (미터)
   * @param pageNo     페이지 번호
   * @param numOfRows  페이지당 조회 건수
   * @param indsLclsCd 상권업종대분류코드 (필터링하지 않으려면 빈 문자열)
   * @param indsMclsCd 상권업종중분류코드 (필터링하지 않으려면 빈 문자열)
   * @param indsSclsCd 상권업종소분류코드 (필터링하지 않으려면 빈 문자열)
   */
  public SemasStoreResponse.Body fetchStoresByRadius(
      Double cx, Double cy, int radius, int pageNo, int numOfRows, String indsLclsCd,
      String indsMclsCd, String indsSclsCd
  ) {
    log.debug("반경 상가업소 조회 요청: cx={}, cy={}, radius={}, pageNo={}, numOfRows={}",
        cx, cy, radius, pageNo, numOfRows);

    SemasStoreResponse response = callApiWithRetry(() ->
        restClient.get()
            .uri(STORE_LIST_IN_RADIUS_URL
                    + "?ServiceKey={serviceKey}&pageNo={pageNo}&numOfRows={numOfRows}"
                    + "&radius={radius}&cx={cx}&cy={cy}"
                    + "&indsLclsCd={indsLclsCd}&indsMclsCd={indsMclsCd}&indsSclsCd={indssclsCd}"
                    + "&type={type}",
                apiKey, pageNo, numOfRows, radius, cx, cy,
                indsLclsCd, indsMclsCd, indsSclsCd, RESPONSE_TYPE_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(SemasStoreResponse.class)
    );

    return response.body();
  }

  /**
   * API 호출 공통 메서드 (재시도 로직 포함)
   */
  private SemasStoreResponse callApiWithRetry(Supplier<SemasStoreResponse> apiCall) {
    for (int attempt = 1; attempt <= RETRY_LIMIT; attempt++) {
      try {
        SemasStoreResponse response = apiCall.get();
        SemasStoreResponse.Header header = response.header();

        // 성공
        if (SUCCESS_CODE.equals(header.resultCode())) {
          return response;
        }

        // 최종 실패
        if (attempt == RETRY_LIMIT) {
          log.error("상가업소 API 호출 최종 실패 (에러코드: {}): {}", header.resultCode(),
              header.resultMsg());
          throw new RuntimeException("Place API 호출 실패: " + header.resultMsg());
        } else {
          log.warn("상가업소 API 호출 실패 (에러코드: {}), 재시도: {}", header.resultCode(),
              header.resultMsg());
        }

      } catch (RestClientException ex) {
        if (attempt == RETRY_LIMIT) {
          log.error("상가업소 API HTTP 호출 실패 (네트워크 오류): {}", ex.getMessage(), ex);
          throw ex;
        }
        log.warn("상가업소 API 네트워크 오류 발생, 재시도");
      }
    }
    throw new RuntimeException("최대 재시도 횟수 초과");
  }
}