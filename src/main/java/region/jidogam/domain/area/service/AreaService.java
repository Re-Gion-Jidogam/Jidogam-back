package region.jidogam.domain.area.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.area.dto.api.AddressInfo;
import region.jidogam.domain.area.dto.api.Sido;
import region.jidogam.domain.area.dto.api.Sigungu;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.exception.AreaNotFoundException;
import region.jidogam.domain.area.parser.AddressParser;
import region.jidogam.domain.area.repository.AreaRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaService {

  private final AreaRepository areaRepository;
  private final AddressParser addressParser;

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

  // 캐시 필요
  public Area getAreaByAddress(String fullAddress) {

    AddressInfo addressInfo = addressParser.parseAddress(fullAddress);

    String sido = addressInfo.sido();
    String sigungu = addressInfo.sigungu();

    return areaRepository.findBySidoAndSigungu(sido, sigungu)
      .orElseThrow(() -> AreaNotFoundException.withSidoAndSigungu(sido, sigungu));
  }
}
