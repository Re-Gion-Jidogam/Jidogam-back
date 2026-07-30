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
import region.jidogam.domain.area.dto.AreaLegacyCodeRegisterRequest;
import region.jidogam.domain.area.dto.AreaLegacyCodeRegisterRequest.LegacyCodeMapping;
import region.jidogam.domain.area.dto.AreaWeightUpdateRequest;
import region.jidogam.domain.area.dto.AreaWeightUpdateRequest.RegionPopulation;
import region.jidogam.domain.area.dto.Sido;
import region.jidogam.domain.area.dto.Sigungu;
import region.jidogam.domain.area.entity.AdministrativeLevel;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.entity.Area.PopulationDeclineCategory;
import region.jidogam.domain.area.entity.AreaLegacyCode;
import region.jidogam.domain.area.exception.AreaNotFoundException;
import region.jidogam.domain.area.exception.InvalidAreaCodeException;
import region.jidogam.domain.area.exception.InvalidWeightException;
import region.jidogam.domain.area.repository.AreaLegacyCodeRepository;
import region.jidogam.domain.area.repository.AreaRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AreaService {

  private static final int SIDO_CODE_LENGTH = 2;
  private static final int SIGUNGU_CODE_LENGTH = 5;

  private final AreaRepository areaRepository;
  private final AreaLegacyCodeRepository areaLegacyCodeRepository;

  @Value("${jidogam.area.weights.normal}")
  private double normalAreaWeight;

  @Value("${jidogam.area.weights.interest}")
  private double interestAreaWeight;

  @Value("${jidogam.area.weights.underserved}")
  private double underservedAreaWeight;

  @Transactional
  public Map<String, Area> saveSido(List<Sido> sidos) {

    sidos.forEach(this::validateSidoCode);

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

    sigungus.forEach(this::validateSigunguCode);

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

  @Transactional
  public void registerLegacyCodes(AreaLegacyCodeRegisterRequest request) {

    List<AreaLegacyCode> legacyCodes = request.legacyCodes().stream()
        .filter(
            mapping -> !areaLegacyCodeRepository.existsByLegacyCode(mapping.legacyCode())) // 중복 체크
        .map(this::toAreaLegacyCode)
        .toList();

    areaLegacyCodeRepository.saveAll(legacyCodes);
    log.debug("레거시 지역 코드 등록 완료 (total: {})", legacyCodes.size());
  }

  @Transactional
  public void updateAreaWeight(AreaWeightUpdateRequest request) {

    if (!request.useDefaultWeights()) {
      validateWeightValues(request.regions());
    }

    for (RegionPopulation region : request.regions()) {

      Area area = getByCode(region.sggCode(), region.getFullName());

      PopulationDeclineCategory newPopulationDeclineCategory = selectPopulationDeclineCategory(
          region);

      double newWeight = request.useDefaultWeights()
          ? selectAreaWeight(region)
          : region.weight();

      area.updateWeight(newWeight);
      area.updatePopulationDeclineCategory(newPopulationDeclineCategory);
    }
  }

  /**
   * 시군구 코드로 Area 조회
   *
   * @param sigunguCode 시군구 코드
   * @param sigunguName 시군구 지명 (log 용)
   * @return
   */
  public Area getByCode(String sigunguCode, String sigunguName) {

    return areaRepository.findByCode(sigunguCode)
        .or(() -> areaLegacyCodeRepository.findByLegacyCode(sigunguCode)
            .map(legacy -> {
              log.info("레거시 코드로 매칭됨: {}({}) -> {}({})", sigunguCode, sigunguName,
                  legacy.getArea().getCode(), legacy.getArea().getName());
              return legacy.getArea();
            }))
        .orElseThrow(() -> AreaNotFoundException.withCode(sigunguCode));
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

  private AreaLegacyCode toAreaLegacyCode(LegacyCodeMapping mapping) {

    Area area = areaRepository.findByCode(mapping.areaCode())
        .orElseThrow(() -> AreaNotFoundException.withCode(mapping.areaCode()));

    log.info("레거시 코드 등록: {}({}) -> {}({}), effectiveFrom={}", mapping.legacyCode(),
        mapping.legacyName(), area.getCode(), area.getName(), mapping.effectiveFrom());

    return AreaLegacyCode.builder()
        .legacyCode(mapping.legacyCode())
        .legacyName(mapping.legacyName())
        .effectiveFrom(mapping.effectiveFrom())
        .area(area)
        .build();
  }

  private void validateSidoCode(Sido sido) {
    if (sido.code().length() != SIDO_CODE_LENGTH) {
      throw InvalidAreaCodeException.withSidoCode(sido.code());
    }
  }

  private void validateSigunguCode(Sigungu sigungu) {
    if (sigungu.code().length() != SIGUNGU_CODE_LENGTH) {
      throw InvalidAreaCodeException.withSigunguCode(sigungu.code());
    }
  }
}
