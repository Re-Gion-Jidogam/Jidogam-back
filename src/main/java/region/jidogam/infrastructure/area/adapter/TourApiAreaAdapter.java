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
import region.jidogam.domain.area.dto.Sido;
import region.jidogam.domain.area.dto.Sigungu;
import region.jidogam.infrastructure.area.dto.LdongCode;
import region.jidogam.infrastructure.area.dto.TourApiResponse;
import region.jidogam.infrastructure.area.port.AreaPort;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourApiAreaAdapter implements AreaPort {

  private static final int RETRY_LIMIT = 3;
  private static final String SUCCESS_CODE = "0000";
  private static final int SIDO_CODE_LENGTH = 2;
  private static final int FULL_SIGUNGU_CODE_LENGTH = 5;

  private final RestClient restClient;

  @Value("${api.area.api-key}")
  private String apiKey;

  /**
   * 지역 API 에서 시도 데이터 가져오기
   */
  @Override
  public List<Sido> getSido() {
    log.debug("시도 정보 요청");

    List<LdongCode> ldongCodes = callApiWithRetry("시도 정보", () ->
        restClient.get()
            .uri("https://apis.data.go.kr/B551011/KorService2/ldongCode2"
                    + "?serviceKey={serviceKey}&MobileOS={mobileOS}&MobileApp={mobileApp}"
                    + "&_type={type}&numOfRows={numOfRows}",
                apiKey, "WEB", "jidogam", "json", "100")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(new ParameterizedTypeReference<TourApiResponse<LdongCode>>() {
            })
    );

    return ldongCodes.stream()
        .map(ldongCode -> new Sido(ldongCode.name(), toSidoCode(ldongCode.code())))
        .toList();
  }

  /**
   * 세종특별자치시처럼 시군구 구분 없이 완전한 시군구 코드(5자리)로 내려오는 경우가 있어,
   * 시도 코드는 항상 앞 2자리로 정규화한다.
   */
  private String toSidoCode(String ldongCode) {
    if (ldongCode.length() > SIDO_CODE_LENGTH) {
      log.info("시도 코드가 예상보다 긴 경우 (세종 등 예외 케이스): code={}", ldongCode);
      return ldongCode.substring(0, SIDO_CODE_LENGTH);
    }
    return ldongCode;
  }

  /**
   * 지역 API 에서 시군구 데이터 가져오기
   */
  @Override
  public List<Sigungu> getSigungu(Sido sido) {
    log.debug("{} 지역 시군구 정보 요청", sido.name());

    List<LdongCode> ldongCodes = callApiWithRetry("시군구 정보", () ->
        restClient.get()
            .uri("https://apis.data.go.kr/B551011/KorService2/ldongCode2"
                    + "?serviceKey={serviceKey}&MobileOS={mobileOS}&MobileApp={mobileApp}"
                    + "&_type={type}&numOfRows={numOfRows}&lDongRegnCd={lDongRegnCd}",
                apiKey, "WEB", "jidogam", "json", "100", sido.code())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(new ParameterizedTypeReference<TourApiResponse<LdongCode>>() {
            })
    );

    return ldongCodes.stream()
        .map(ldongCode -> new Sigungu(ldongCode.name(),
            toFullSigunguCode(sido.code(), ldongCode.code())))
        .toList();
  }

  /**
   * 세종시처럼 시군구 구분 없이 이미 5자리 완전한 코드로 내려오는 경우 그대로 사용한다.
   */
  private String toFullSigunguCode(String sidoCode, String ldongCode) {
    if (ldongCode.length() == FULL_SIGUNGU_CODE_LENGTH) {
      log.info("이미 완전한 시군구 코드 (세종 등 예외 케이스): code={}", ldongCode);
      return ldongCode;
    }
    return sidoCode + ldongCode;
  }

  /**
   * API 호출 공통 메서드 (재시도 로직 포함)
   */
  private <T> List<T> callApiWithRetry(String apiName,
      Supplier<TourApiResponse<T>> apiCall) {

    for (int attempt = 1; attempt <= RETRY_LIMIT; attempt++) {
      try {
        TourApiResponse<T> response = apiCall.get();
        TourApiResponse.Header header = response.response()
            .header();

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
