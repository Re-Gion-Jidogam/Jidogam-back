package region.jidogam.domain.place.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import region.jidogam.domain.place.dto.ExternalPlaceData;
import region.jidogam.domain.place.dto.PlaceNearByRequest;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.dto.PlaceStoreInitRequest;
import region.jidogam.domain.place.dto.PlaceStoreInitResponse;
import region.jidogam.domain.place.dto.PlaceStoreInitResult;
import region.jidogam.domain.place.entity.Place.Source;
import region.jidogam.infrastructure.place.port.PlacePort;

/**
 * 공공데이터(SEMAS) 기반 장소 적재/동기화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceInitService {

  private static final int NEARBY_SYNC_RADIUS_METERS = 1000;

  private final PlacePort placePort;
  private final PlaceService placeService;

  @Value("${jidogam.place.nearby.external-fetch-enabled:true}")
  private boolean externalFetchEnabled;

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

  /**
   * 사용자 위치 주변 상가업소 정보를 공공데이터에서 동기화한 뒤, 주변 장소 목록 조회
   */
  public List<PlaceResponse> nearbyListWithSync(PlaceNearByRequest request, UUID userId) {
    syncNearbyPlaces(request.lat(), request.lon(), request.limit());
    return placeService.nearbyList(request, userId);
  }

  /**
   * 사용자 위치 주변 상가업소 정보를 공공데이터에서 조회해 장소 데이터에 반영
   * <p>
   * 외부 API 호출 자체가 실패하거나 개별 장소 적재가 실패해도 예외를 전파하지 않고 무시
   * 데이터가 충분히 쌓이면 jidogam.place.nearby.external-fetch-enabled 설정을 꺼서 비활성화 가능
   */
  public void syncNearbyPlaces(Double lat, Double lon, int limit) {
    if (!externalFetchEnabled) {
      return;
    }

    try {
      List<ExternalPlaceData> storeData = placePort.getStoresByRadius(
          lat, lon, NEARBY_SYNC_RADIUS_METERS, limit, null, null, null);

      storeData.forEach(store -> {
        try {
          placeService.upsertPlace(store, Source.SEMAS);
        } catch (Exception e) {
          log.warn("주변 장소 동기화 실패: externalId={}, reason={}",
              store.externalId(), e.getMessage());
        }
      });
    } catch (Exception e) {
      log.warn("주변 장소 외부 데이터 조회 실패, 기존 데이터로만 응답: {}", e.getMessage());
    }
  }
}
