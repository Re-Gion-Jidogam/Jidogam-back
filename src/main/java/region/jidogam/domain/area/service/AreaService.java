package region.jidogam.domain.area.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.area.dto.AreaWeightUpdateRequest;
import region.jidogam.domain.area.dto.AreaWeightUpdateRequest.RegionPopulation;
import region.jidogam.domain.area.dto.api.AddressInfo;
import region.jidogam.domain.area.dto.api.Sido;
import region.jidogam.domain.area.dto.api.Sigungu;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.entity.Area.AreaType;
import region.jidogam.domain.area.exception.AreaNotFoundException;
import region.jidogam.domain.area.exception.InvalidWeightException;
import region.jidogam.domain.area.parser.AddressParser;
import region.jidogam.domain.area.repository.AreaRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaService {

  private final AreaRepository areaRepository;
  private final AddressParser addressParser;

  @Value("${jidogam.area.weights.normal}")
  private double normalAreaWeight;

  @Value("${jidogam.area.weights.interest}")
  private double interestAreaWeight;

  @Value("${jidogam.area.weights.underserved}")
  private double underservedAreaWeight;

  @Transactional
  public void saveAreaData(Sido sido, List<Sigungu> sigungus) {

    List<Area> areas = sigungus.stream()
        .filter(sigungu -> !areaRepository.existsBySigunguCode(sigungu.code())) // 중복 체크
        .map(sigungu -> Area.builder()
            .sido(sido.addressName())
            .sigungu(sigungu.addressName())
            .sigunguCode(sigungu.code())
            .type(AreaType.NORMAL)
            .weight(normalAreaWeight)
            .weightUpdatedAt(LocalDateTime.now())
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

  @Transactional
  public void updateAreaSettings(AreaWeightUpdateRequest request) {

    if (!request.useDefaultWeights()) {
      validateWeightValues(request.regions());
    }

    for (RegionPopulation region : request.regions()) {

      Area area = getAreaByAddress(region.getFullName());

      AreaType newAreaType = selectAreaType(region);

      double newWeight = request.useDefaultWeights()
          ? selectAreaWeight(region)
          : region.weight();

      area.updateWeight(newWeight);
      area.updateType(newAreaType);
    }
  }

  private void validateWeightValues(List<RegionPopulation> regions) {
    for (RegionPopulation region : regions) {
      if (region.weight() == null) {
        throw new InvalidWeightException(region.getFullName());
      }
    }
  }

  private AreaType selectAreaType(RegionPopulation region) {

    if (region.isPopulationDecreaseRegion().getValue()) {
      return AreaType.UNDERSERVED;
    }
    if (region.isPopulationDecreaseInterestRegion().getValue()) {
      return AreaType.INTEREST;
    }
    return AreaType.NORMAL;
  }

  private double selectAreaWeight(RegionPopulation region) {

    if (region.isPopulationDecreaseRegion().getValue()) {
      return underservedAreaWeight;
    }
    if (region.isPopulationDecreaseInterestRegion().getValue()) {
      return interestAreaWeight;
    }
    return normalAreaWeight;
  }
}
