package region.jidogam.infrastructure.area.port;

import java.util.List;
import region.jidogam.domain.area.dto.Sido;
import region.jidogam.domain.area.dto.Sigungu;

public interface AreaPort {

  /**
   * 시도 데이터 가져오기
   */
  List<Sido> getSido();

  /**
   * 시군구 데이터 가져오기
   */
  List<Sigungu> getSigungu(Sido sido);
}
