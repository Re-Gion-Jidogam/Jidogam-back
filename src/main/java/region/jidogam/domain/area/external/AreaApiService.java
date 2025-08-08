package region.jidogam.domain.area.external;

import java.util.List;
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

  private final RestClient restClient;

  @Value("${api.area.access-token}")
  private String accessToken;

  @Value("${api.area.service-id}")
  private String serviceId;

  @Value("${api.area.secret-key}")
  private String secretKey;

  /**
   * 지역 API 에서 시도 데이터 가져오기
   */
  public List<Sido> getSido() {

    log.info("시도 정보 요청 (token: {})", accessToken);

    try {
      AreaApiResponse<List<Sido>> response = restClient.get()
        .uri(
          "https://sgisapi.kostat.go.kr/OpenAPI3/addr/stage.json?accessToken={accessToken}",
          accessToken
        )
        .accept(MediaType.APPLICATION_JSON)
        .retrieve() // 요청 전송
        .body(new ParameterizedTypeReference<AreaApiResponse<List<Sido>>>() {
        });

      if (response.errCd().equals("-401")) {
        log.error("인증 정보가 유효하지 않음. ({})", response.errMsg());
        throw new RuntimeException("Area API 인증 정보가 유효하지 않음.");
      }

      return response.result();

    } catch (RestClientException ex) { // timeout, DNS, 커넥션 오류 등
      log.error("HTTP call failed (transport level): {}", ex.getMessage(), ex);
      throw ex;
    }
  }

  /**
   * 지역 API 에서 시군구 데이터 가져오기
   */
  public List<Sigungu> getSigungu(Sido sido) {

    log.info("{} 지역 시군구 정보 요청", sido.addressName());

    try {
      AreaApiResponse<List<Sigungu>> response = restClient.get()
        .uri(
          "https://sgisapi.kostat.go.kr/OpenAPI3/addr/stage.json"
            + "?accessToken={accessToken}&cd={sidoCode}", accessToken, sido.code()
        )
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(new ParameterizedTypeReference<AreaApiResponse<List<Sigungu>>>() {
        });

      if (response.errCd().equals("-401")) {
        log.error("인증 정보가 유효하지 않음. ({})", response.errMsg());
        throw new RuntimeException("Area API 인증 정보가 유효하지 않음.");
      }

      return response.result();

    } catch (RestClientException ex) {
      log.error("HTTP call failed (transport level): {}", ex.getMessage(), ex);
      throw ex;
    }
  }

  /**
   * 액세스 토큰 발급 (임시)
   */
  public String getAccessToken() {
    try {
      AreaApiResponse<AuthResponse> result = restClient.get()
        .uri(
          "https://sgisapi.kostat.go.kr/OpenAPI3/auth/authentication.json"
            + "?consumer_key={serviceId}&consumer_secret={secretKey}", serviceId, secretKey
        )
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(new ParameterizedTypeReference<AreaApiResponse<AuthResponse>>() {
        });

      log.info("토큰 발급: {}", accessToken);
      return result.result().accessToken();

    } catch (RestClientException ex) {
      log.error("HTTP call failed (transport level): {}", ex.getMessage(), ex);
      throw ex;
    }
  }

}
