package region.jidogam.domain.place.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import region.jidogam.domain.place.dto.ExternalPlaceData;
import region.jidogam.domain.place.dto.PlaceStoreInitRequest;
import region.jidogam.domain.place.dto.PlaceStoreInitResponse;
import region.jidogam.domain.place.dto.PlaceStoreInitResult;
import region.jidogam.domain.place.entity.Place.Source;
import region.jidogam.infrastructure.place.port.PlacePort;

/**
 * 공공데이터(SEMAS) 기반 장소 초기 적재
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceInitService {

  private final PlacePort placePort;
  private final PlaceService placeService;

  /**
   * 주어진 시군구코드 목록에 대해 상가업소 정보를 가져와 장소 데이터 적재
   * <p>
   * 개별 장소 적재에 실패해도 나머지 적재는 계속 진행하고, 시군구별 성공/실패 건수를 집계해서 반환
   */
  public PlaceStoreInitResponse initializeStoreData(
      PlaceStoreInitRequest request
  ) {

    List<PlaceStoreInitResult> results = request.sigunguCodes().stream()
        .map(sigunguCode -> initializeStoreDataBySigungu(sigunguCode, request))
        .toList();

    log.info("장소 초기 적재 완료 (시군구 {}개)", request.sigunguCodes().size());

    return PlaceStoreInitResponse.of(results);
  }

  private PlaceStoreInitResult initializeStoreDataBySigungu(
      String sigunguCode, PlaceStoreInitRequest request
  ) {
    List<ExternalPlaceData> storeData = placePort.getStoresBySigunguCode(
        sigunguCode, request.limit(), request.largeCategoryCode(), request.mediumCategoryCode(),
        request.smallCategoryCode());

    int succeededCount = 0;
    int failedCount = 0;

    for (ExternalPlaceData store : storeData) {
      try {
        placeService.upsertPlace(store, Source.SEMAS);
        succeededCount++;
      } catch (Exception e) {
        failedCount++;
        log.warn("장소 적재 실패: sigunguCode={}, externalId={}, reason={}",
            sigunguCode, store.externalId(), e.getMessage());
      }
    }

    log.info("{} 상가업소 정보 {}건 중 {}건 적재 완료 ({}건 실패)",
        sigunguCode, storeData.size(), succeededCount, failedCount);

    return PlaceStoreInitResult.builder()
        .sigunguCode(sigunguCode)
        .attemptedCount(storeData.size())
        .succeededCount(succeededCount)
        .failedCount(failedCount)
        .build();
  }
}
