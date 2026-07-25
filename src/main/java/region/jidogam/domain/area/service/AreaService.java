package region.jidogam.domain.area.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.area.dto.AreaWeightUpdateRequest;
import region.jidogam.domain.area.dto.AreaWeightUpdateRequest.RegionPopulation;
import region.jidogam.domain.area.dto.Sido;
import region.jidogam.domain.area.dto.Sigungu;
import region.jidogam.domain.area.entity.AdministrativeLevel;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.entity.Area.PopulationDeclineCategory;
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
  public Map<String, Area> saveSido(List<Sido> sidos) {

    List<Area> newAreas = sidos.stream()
        .filter(sido -> !areaRepository.existsByParentIsNullAndCode(sido.code()))
        .map(sido -> Area.builder()
            .name(sido.name())
            .code(sido.code())
            .administrativeLevel(AdministrativeLevel.SIDO)
            .build())
        .toList();

    areaRepository.saveAll(newAreas);
    log.debug("모든 시도 저장 완료 (total: {})", sidos.size());

    return sidos.stream()
        .map(sido -> areaRepository.findByCode(sido.code())
            .orElseThrow(() -> AreaNotFoundException.withCode(sido.code())))
        .collect(Collectors.toMap(Area::getCode, Function.identity()));
  }

  @Transactional
  public void saveSigungu(Area sido, List<Sigungu> sigungus) {

    List<Area> areas = sigungus.stream()
        .filter(sigungu -> !areaRepository.existsByParent_IdAndCode(sido.getId(),
            sigungu.code())) // 중복 체크
        .map(sigungu -> Area.builder()
            .name(sigungu.name())
            .code(sigungu.code())
            .parent(sido)
            .administrativeLevel(AdministrativeLevel.SIGUNGU)
            .populationDeclineCategory(PopulationDeclineCategory.NORMAL)
            .weight(normalAreaWeight)
            .weightUpdatedAt(LocalDateTime.now())
            .build())
        .toList();

    areaRepository.saveAll(areas);
    log.debug("{} 지역 시군구 저장 완료 (total: {})", sido.getName(), sigungus.size());
  }

  // 캐시 필요
  public Area getAreaByAddress(String fullAddress) {

    // todo: 여기를 alias db로 변경 -> 지역 데이터에 이미 있으니까 굳이 필요 없을 듯
    // AddressInfo addressInfo = addressParser.parseAddress(fullAddress);

    //    String sido = addressInfo.sido();
    //    String sigungu = addressInfo.sigungu();

    //return areaRepository.findByParentAndCode(sido, sigungu)
    //    .orElseThrow(() -> AreaNotFoundException.withSidoAndSigungu(sido, sigungu));
    return null;
  }

  @Transactional
  public void updateAreaSettings(AreaWeightUpdateRequest request) {

    if (!request.useDefaultWeights()) {
      validateWeightValues(request.regions());
    }

    for (RegionPopulation region : request.regions()) {

      Area area = getAreaByAddress(region.getFullName());

      PopulationDeclineCategory newPopulationDeclineCategory = selectPopulationDeclineCategory(
          region);

      double newWeight = request.useDefaultWeights()
          ? selectAreaWeight(region)
          : region.weight();

      area.updateWeight(newWeight);
      area.updatePopulationDeclineCategory(newPopulationDeclineCategory);
    }
  }

  private void validateWeightValues(List<RegionPopulation> regions) {
    for (RegionPopulation region : regions) {
      if (region.weight() == null) {
        throw new InvalidWeightException(region.getFullName());
      }
    }
  }

  private PopulationDeclineCategory selectPopulationDeclineCategory(RegionPopulation region) {

    if (region.isPopulationDecreaseRegion().getValue()) {
      return PopulationDeclineCategory.UNDERSERVED;
    }
    if (region.isPopulationDecreaseInterestRegion().getValue()) {
      return PopulationDeclineCategory.INTEREST;
    }
    return PopulationDeclineCategory.NORMAL;
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
