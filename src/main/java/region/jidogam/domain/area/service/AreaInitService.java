package region.jidogam.domain.area.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import region.jidogam.domain.area.dto.Sido;
import region.jidogam.domain.area.dto.Sigungu;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.infrastructure.area.port.AreaPort;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaInitService {

  private final AreaPort areaApiClient;
  private final AreaService areaService;

  public void initializeAreaData() {

    // 1. 시도 목록 API 호출
    List<Sido> sidos = areaApiClient.getSido();

    // 2. 시도 저장 후, code로 저장된 Area 엔티티 조회
    Map<String, Area> sidoAreasByCode = areaService.saveSido(sidos);

    // 3. 각 시도별 시군구 목록 API 호출 후, 부모(시도) 엔티티와 함께 저장
    sidos.forEach(sido -> {
      List<Sigungu> sigungus = areaApiClient.getSigungu(sido);
      Area sidoArea = sidoAreasByCode.get(sido.code());
      areaService.saveSigungu(sidoArea, sigungus);
    });

    log.info("모든 지역 정보 저장 완료");
  }
}
