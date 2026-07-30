package region.jidogam.infrastructure.area.adapter;

import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import region.jidogam.infrastructure.area.dto.LdongCode;
import region.jidogam.infrastructure.area.dto.TourApiResponse;

/**
 * TourAPI(한국관광공사) 법정동코드(ldongCode2) 조회 클라이언트.
 * <p>
 * API 요청/응답만 담당하며, 도메인 변환은 이 클라이언트를 사용하는 어댑터에서 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TourApiAreaClient {

  private static final int RETRY_LIMIT = 3;
  private static final String SUCCESS_CODE = "0000";
  private static final String LDONG_CODE_URL =
      "https://apis.data.go.kr/B551011/KorService2/ldongCode2";
  private static final String MOBILE_OS = "WEB";
  private static final String MOBILE_APP = "jidogam";
  private static final String RESPONSE_TYPE = "json";
  private static final String NUM_OF_ROWS = "100";

  private final RestClient restClient;

  @Value("${api.area.api-key}")
  private String apiKey;

  /**
   * 법정동코드를 조회한다. lDongRegnCd가 없으면(빈 문자열) 시도 목록을,
   * 있으면 해당 시도 하위 시군구 목록을 반환한다.
   *
   * @param lDongRegnCd 상위 시도코드로 필터링 (시도 목록 조회 시 빈 문자열)
   */
  public List<LdongCode> fetchLdongCodes(String lDongRegnCd) {
    log.debug("법정동코드 조회 요청: lDongRegnCd={}", lDongRegnCd);

    return callApiWithRetry("법정동코드", () ->
        restClient.get()
            .uri(LDONG_CODE_URL
                    + "?serviceKey={serviceKey}&MobileOS={mobileOS}&MobileApp={mobileApp}"
                    + "&_type={type}&numOfRows={numOfRows}&lDongRegnCd={lDongRegnCd}",
                apiKey, MOBILE_OS, MOBILE_APP, RESPONSE_TYPE, NUM_OF_ROWS, lDongRegnCd)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(new ParameterizedTypeReference<TourApiResponse<LdongCode>>() {
            })
    );
  }

  /**
   * API 호출 공통 메서드 (재시도 로직 포함)
   */
  private List<LdongCode> callApiWithRetry(String apiName,
      Supplier<TourApiResponse<LdongCode>> apiCall) {

    for (int attempt = 1; attempt <= RETRY_LIMIT; attempt++) {
      try {
        TourApiResponse<LdongCode> response = apiCall.get();
        TourApiResponse.Header header = response.response().header();

        // 성공
        if (SUCCESS_CODE.equals(header.resultCode())) {
          return response.response().body().items().item();
        }

        // 최종 실패
        if (attempt == RETRY_LIMIT) {
          log.error("{} API 호출 최종 실패 (에러코드: {}): {}", apiName, header.resultCode(),
              header.resultMsg());
          throw new RuntimeException("Area API 호출 실패: " + header.resultMsg());
        } else {
          log.warn("{} API 호출 실패 (에러코드: {}), 재시도: {}", apiName, header.resultCode(),
              header.resultMsg());
        }

      } catch (RestClientException ex) {
        if (attempt == RETRY_LIMIT) {
          log.error("{} HTTP 호출 실패 (네트워크 오류): {}", apiName, ex.getMessage(), ex);
          throw ex;
        }
        log.warn("{} 네트워크 오류 발생, 재시도", apiName);
      }
    }
    throw new RuntimeException("최대 재시도 횟수 초과");
  }
}
