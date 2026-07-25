package region.jidogam.domain.area.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AreaLegacyCodeRegisterRequest(

    List<LegacyCodeMapping> legacyCodes
) {

  public record LegacyCodeMapping(

      @NotNull(message = "이전 코드 값은 필수입니다.")
      String legacyCode,

      @NotNull(message = "이전 구역명은 필수입니다.")
      String legacyName,

      @NotNull(message = "새로운 코드 값은 필수입니다.")
      String areaCode
  ) {

  }
}
