package region.jidogam.domain.area.external;

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
import region.jidogam.domain.area.dto.api.AreaApiResponse;
import region.jidogam.domain.area.dto.api.AuthResponse;
import region.jidogam.domain.area.dto.api.Sido;
import region.jidogam.domain.area.dto.api.Sigungu;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaApiService {

  private static final int RETRY_LIMIT = 3;
  private static final String SUCCESS_CODE = "0";
  private static final String AUTH_INVALID_CODE = "-401";

  private final RestClient restClient;

  @Value("${api.area.service-id}")
  private String serviceId;

  @Value("${api.area.secret-key}")
  private String secretKey;

  private String accessToken;

  /**
   * 지역 API 에서 시도 데이터 가져오기
   */
  public List<Sido> getSido() {
    log.info("시도 정보 요청");

    return callApiWithRetry("시도 정보", () ->
      restClient.get()
        .uri("https://sgisapi.kostat.go.kr/OpenAPI3/addr/stage.json?accessToken={accessToken}",
          accessToken)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(new ParameterizedTypeReference<AreaApiResponse<List<Sido>>>() {
        })
    ).result();
  }

  /**
   * 지역 API 에서 시군구 데이터 가져오기
   */
  public List<Sigungu> getSigungu(Sido sido) {
    log.info("{} 지역 시군구 정보 요청", sido.addressName());

    return callApiWithRetry(sido.addressName() + " 시군구 정보", () ->
      restClient.get()
        .uri(
          "https://sgisapi.kostat.go.kr/OpenAPI3/addr/stage.json?accessToken={accessToken}&cd={sidoCode}",
          accessToken, sido.code())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(new ParameterizedTypeReference<AreaApiResponse<List<Sigungu>>>() {
        })
    ).result();
  }

  /**
   * API 호출 공통 메서드 (재시도 로직 포함)
   */
  private <T> AreaApiResponse<T> callApiWithRetry(String apiName,
    Supplier<AreaApiResponse<T>> apiCall) {
    if (accessToken == null) {
      refreshToken();
    }

    for (int attempt = 1; attempt <= RETRY_LIMIT; attempt++) {
      try {
        AreaApiResponse<T> response = apiCall.get();

        // 성공
        if (SUCCESS_CODE.equals(response.errCd())) {
          return response;
        }

        // 최종 실패
        if (attempt == RETRY_LIMIT) {
          log.error("{} API 호출 최종 실패 (에러코드: {}): {}", apiName, response.errCd(), response.errMsg());
          throw new RuntimeException("Area API 호출 실패: " + response.errMsg());
        }

        // 재시도
        if (AUTH_INVALID_CODE.equals(response.errCd())) {
          log.warn("인증 정보가 유효하지 않음. 토큰 재발급 시도 ({})", response.errMsg());
          refreshToken();
        } else {
          log.warn("{} API 호출 실패 (에러코드: {}), 재시도: {}", apiName, response.errCd(),
            response.errMsg());
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

  /**
   * 액세스 토큰 발급
   */
  private void refreshToken() {
    AreaApiResponse<AuthResponse> response = restClient.get()
      .uri(
        "https://sgisapi.kostat.go.kr/OpenAPI3/auth/authentication.json"
          + "?consumer_key={serviceId}&consumer_secret={secretKey}", serviceId, secretKey
      )
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .body(new ParameterizedTypeReference<AreaApiResponse<AuthResponse>>() {
      });

    log.error("{} : {}", response.errCd(), response.errMsg());

    if (response == null || response.result() == null || response.result().accessToken() == null) {
      throw new RuntimeException("토큰 발급 실패: 응답 비정상");
    }
    this.accessToken = response.result().accessToken();
    log.info("토큰 발급 성공: {}", accessToken);
  }

}
