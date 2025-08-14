package region.jidogam.domain.area.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import region.jidogam.domain.area.dto.api.AddressInfo;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressParser {

  private final SidoNormalizer sidoNormalizer;

  /**
   * 전체 주소 문자열을 시도/시군구 정보로 파싱
   */
  public AddressInfo parseAddress(String fullAddress) {

    if (fullAddress == null) {
      return null;
    }

    // 분리
    String[] parts = fullAddress.strip().split("\\s+");
    if (parts.length < 2) {
      return null;
    }

    // 시도 정규화
    String sido = sidoNormalizer.normalize(parts[0]);

    // 시군구 추출
    String sigungu = extractSigungu(parts);
    if ("세종특별자치시".equals(sido)) {
      sigungu = "세종시";
    }

    return new AddressInfo(sido, sigungu);
  }

  /**
   * 주소 배열에서 시군구 정보 추출하여
   * 최대 3번째 요소까지만 검사
   */
  private String extractSigungu(String[] parts) {
    StringBuilder result = new StringBuilder();

    // 최대 3번째 요소까지만 확인
    int limit = Math.min(parts.length, 3);

    for (int i = 1; i < limit; i++) {
      String part = parts[i];

      // 시는 추가하고 다음 구 확인을 위해 계속
      if (part.endsWith("군") || part.endsWith("구")) {
        if (!result.isEmpty()) {
          result.append(" ");
        }
        result.append(part);
        break;

      } else if (part.endsWith("시")) {
        result.append(part);
      }
    }

    return result.isEmpty() ? null : result.toString();
  }

}
