package region.jidogam.domain.area.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import region.jidogam.domain.area.dto.api.Sido;
import region.jidogam.domain.area.dto.api.Sigungu;
import region.jidogam.domain.area.external.AreaApiService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaInitService {

  private final AreaApiService areaApiService;
  private final AreaService areaService;

  public void initializeAreaData() {

    // 1. 시도 목록 API 호출
    List<Sido> sidos = areaApiService.getSido();

    // 2. 각 시도별 시군구 목록 API 호출 후 저장
    sidos.forEach(sido -> {
      List<Sigungu> sigungus = areaApiService.getSigungu(sido);
      areaService.saveAreaDate(sido, sigungus);
    });

    log.info("모든 지역 정보 저장 완료");
  }
}
