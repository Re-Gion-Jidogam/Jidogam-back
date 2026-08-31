package region.jidogam.infrastructure.area.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import region.jidogam.domain.area.dto.Sido;
import region.jidogam.domain.area.dto.Sigungu;
import region.jidogam.infrastructure.area.dto.LdongCode;
import region.jidogam.infrastructure.area.port.AreaPort;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourApiAreaAdapter implements AreaPort {

  private static final int SIDO_CODE_LENGTH = 2;
  private static final int FULL_SIGUNGU_CODE_LENGTH = 5;
  private static final String NO_PARENT_SIDO_CODE = "";
  private static final String SEJONG_SIDO_CODE = "36";
  private static final String SEJONG_SIGUNGU_CODE = "36110";

  private final TourApiAreaClient tourApiAreaClient;

  /**
   * 지역 API 에서 시도 데이터 가져오기
   */
  @Override
  public List<Sido> getSido() {

    List<LdongCode> ldongCodes = tourApiAreaClient.fetchLdongCodes(NO_PARENT_SIDO_CODE);

    return ldongCodes.stream()
        .map(ldongCode -> new Sido(ldongCode.name(), toSidoCode(ldongCode.code())))
        .toList();
  }

  /**
   * 지역 API 에서 시군구 데이터 가져오기
   * <p>
   * 세종특별자치시는 하위 시군구 구분이 없어(자기 자신이 유일한 시군구), API를 다시 호출하지 않고
   * 실제 행정구역코드(36110)로 자기 자신을 유일한 시군구로 반환한다.
   */
  @Override
  public List<Sigungu> getSigungu(Sido sido) {

    if (SEJONG_SIDO_CODE.equals(sido.code())) {
      log.info("세종특별자치시는 시군구 구분이 없어 자기 자신을 유일한 시군구로 사용.");
      return List.of(new Sigungu(sido.name(), SEJONG_SIGUNGU_CODE));
    }

    List<LdongCode> ldongCodes = tourApiAreaClient.fetchLdongCodes(sido.code());

    return ldongCodes.stream()
        .map(ldongCode -> new Sigungu(ldongCode.name(),
            toFullSigunguCode(sido.code(), ldongCode.code())))
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
   * 세종시처럼 시군구 구분 없이 이미 5자리 완전한 코드로 내려오는 경우 그대로 사용한다.
   */
  private String toFullSigunguCode(String sidoCode, String ldongCode) {

    if (ldongCode.length() == FULL_SIGUNGU_CODE_LENGTH) {
      log.info("이미 완전한 시군구 코드 (세종 등 예외 케이스): code={}", ldongCode);
      return ldongCode;
    }
    return sidoCode + ldongCode;
  }
}
