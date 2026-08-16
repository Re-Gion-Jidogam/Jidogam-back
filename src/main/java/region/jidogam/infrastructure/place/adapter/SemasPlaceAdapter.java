package region.jidogam.infrastructure.place.adapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import region.jidogam.domain.place.dto.ExternalPlaceData;
import region.jidogam.infrastructure.place.dto.SemasStoreItem;
import region.jidogam.infrastructure.place.dto.SemasStoreResponse;
import region.jidogam.infrastructure.place.port.PlacePort;

@Slf4j
@Service
@RequiredArgsConstructor
public class SemasPlaceAdapter implements PlacePort {

  private static final int DEFAULT_FETCH_LIMIT = 1000;
  private static final String NO_FILTER = "";

  private final SemasPlaceClient semasPlaceClient;

  /**
   * 시군구코드 기준 상가업소 목록을 최대 limit건까지 조회한다.
   * limit이 페이지 단위(DEFAULT_FETCH_LIMIT)를 넘으면 여러 페이지로 나눠서 요청한다.
   */
  @Override
  public List<ExternalPlaceData> getStoresBySigunguCode(
      String sigunguCode, int limit,
      String largeCategoryCode, String mediumCategoryCode, String smallCategoryCode
  ) {
    log.debug("{} 상가업소 정보 요청 (limit={}, largeCategoryCode={}, mediumCategoryCode={}, "
            + "smallCategoryCode={})",
        sigunguCode, limit, largeCategoryCode, mediumCategoryCode, smallCategoryCode);

    if (limit <= 0) {
      return List.of();
    }

    String indsLclsCd = Objects.requireNonNullElse(largeCategoryCode, NO_FILTER);
    String indsMclsCd = Objects.requireNonNullElse(mediumCategoryCode, NO_FILTER);
    String indsSclsCd = Objects.requireNonNullElse(smallCategoryCode, NO_FILTER);

    List<ExternalPlaceData> storeData = new ArrayList<>();
    int numOfRows = Math.min(DEFAULT_FETCH_LIMIT, limit);
    int pageNo = 1;
    int totalCount;

    do {
      SemasStoreResponse.Body body = semasPlaceClient.fetchStoresBySigunguCode(
          sigunguCode, pageNo, numOfRows, indsLclsCd, indsMclsCd, indsSclsCd);

      storeData.addAll(body.items().stream()
          .map(this::toExternalPlaceData)
          .toList());

      totalCount = body.totalCount();
      pageNo++;
    } while (storeData.size() < limit && storeData.size() < totalCount);

    if (storeData.size() > limit) {
      storeData = new ArrayList<>(storeData.subList(0, limit));
    }

    log.info("{} 상가업소 정보 {}건 조회 완료", sigunguCode, storeData.size());
    return storeData;
  }

  /**
   * 좌표 기준 반경 내 상가업소 목록을 최대 limit건까지 조회한다.
   * limit이 페이지 단위(DEFAULT_FETCH_LIMIT)를 넘으면 여러 페이지로 나눠서 요청한다.
   * <p>
   * SEMAS API는 cx=경도, cy=위도 순서로 받으므로 여기서 순서를 맞춰 호출한다.
   */
  @Override
  public List<ExternalPlaceData> getStoresByRadius(
      Double lat, Double lon, int radiusMeters, int limit,
      String largeCategoryCode, String mediumCategoryCode, String smallCategoryCode
  ) {
    log.debug("반경 {}m 상가업소 정보 요청 (lat={}, lon={}, limit={})", radiusMeters, lat, lon, limit);

    if (limit <= 0) {
      return List.of();
    }

    String indsLclsCd = Objects.requireNonNullElse(largeCategoryCode, NO_FILTER);
    String indsMclsCd = Objects.requireNonNullElse(mediumCategoryCode, NO_FILTER);
    String indsSclsCd = Objects.requireNonNullElse(smallCategoryCode, NO_FILTER);

    List<ExternalPlaceData> storeData = new ArrayList<>();
    int numOfRows = Math.min(DEFAULT_FETCH_LIMIT, limit);
    int pageNo = 1;
    int totalCount;

    do {
      SemasStoreResponse.Body body = semasPlaceClient.fetchStoresByRadius(
          lon, lat, radiusMeters, pageNo, numOfRows, indsLclsCd, indsMclsCd,
          indsSclsCd);

      storeData.addAll(body.items().stream()
          .map(this::toExternalPlaceData)
          .toList());

      totalCount = body.totalCount();
      pageNo++;
    } while (storeData.size() < limit && storeData.size() < totalCount);

    if (storeData.size() > limit) {
      storeData = new ArrayList<>(storeData.subList(0, limit));
    }

    log.info("반경 {}m 상가업소 정보 {}건 조회 완료", radiusMeters, storeData.size());
    return storeData;
  }

  private ExternalPlaceData toExternalPlaceData(SemasStoreItem item) {
    return ExternalPlaceData.builder()
        .externalId(item.bizesId())
        .placeName(item.bizesNm() + Objects.requireNonNullElse(item.brchNm(), ""))
        .jibunAddress(item.lnoAdr())
        .roadAddress(item.rdnmAdr())
        .sidoName(item.ctprvnNm())
        .sidoCode(item.ctprvnCd())
        .sigunguName(item.signguNm())
        .sigunguCode(item.signguCd())
        .categoryCode(item.indsSclsCd())
        .categoryName(item.indsSclsNm())
        .y(item.lat())
        .x(item.lon())
        .fetchedAt(LocalDateTime.now())
        .build();
  }
}