package region.jidogam.infrastructure.place.port;

import java.util.List;
import region.jidogam.domain.place.dto.ExternalPlaceData;

public interface PlacePort {

  /**
   * 시군구코드 기준으로 상가업소 목록을 최대 limit건 조회
   *
   * @param largeCategoryCode  업종 대분류코드로 필터링 (전체 조회 시 null 또는 빈 문자열)
   * @param mediumCategoryCode 업종 중분류코드로 필터링 (전체 조회 시 null 또는 빈 문자열)
   * @param smallCategoryCode  업종 소분류코드로 필터링 (전체 조회 시 null 또는 빈 문자열)
   */
  List<ExternalPlaceData> getStoresBySigunguCode(
      String sigunguCode, int limit,
      String largeCategoryCode, String mediumCategoryCode, String smallCategoryCode);

  /**
   * 좌표 기준 반경 내 상가업소 목록을 최대 limit건 조회
   *
   * @param radiusMeters       검색 반경 (미터)
   * @param largeCategoryCode  업종 대분류코드로 필터링 (전체 조회 시 null 또는 빈 문자열)
   * @param mediumCategoryCode 업종 중분류코드로 필터링 (전체 조회 시 null 또는 빈 문자열)
   * @param smallCategoryCode  업종 소분류코드로 필터링 (전체 조회 시 null 또는 빈 문자열)
   */
  List<ExternalPlaceData> getStoresByRadius(
      Double lat, Double lon, int radiusMeters, int limit,
      String largeCategoryCode, String mediumCategoryCode, String smallCategoryCode);
}