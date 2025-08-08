package region.jidogam.domain.area.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.area.dto.api.Sido;
import region.jidogam.domain.area.dto.api.Sigungu;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.external.AreaApiService;
import region.jidogam.domain.area.repository.AreaRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaService {

  private final AreaApiService areaApiService;
  private final AreaRepository areaRepository;

  @Transactional
  public void saveAreaDate(Sido sido, List<Sigungu> sigungus) {

    List<Area> areas = sigungus.stream()
      .filter(sigungu -> !areaRepository.existsBySigunguCode(sigungu.code())) // 중복 체크
      .map(sigungu -> Area.builder()
        .sido(sido.addressName())
        .sigungu(sigungu.addressName())
        .sigunguCode(sigungu.code())
        .weight(1)
        .build())
      .toList();

    areaRepository.saveAll(areas);
    log.info("{} 지역 시군구 저장 완료 (total: {})", sido.addressName(), sigungus.size());
  }
}
