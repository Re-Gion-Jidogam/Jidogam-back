package region.jidogam.domain.area.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import region.jidogam.domain.area.dto.AreaLegacyCodeRegisterRequest;
import region.jidogam.domain.area.dto.AreaWeightUpdateRequest;

@Tag(name = "Area", description = "지역 관련 API (관리자 전용)")
public interface AreaApi {

  @Operation(summary = "지역 데이터 초기화", description = "지역 데이터를 초기화합니다. (관리자 전용)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "지역 데이터 초기화 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  ResponseEntity<Void> fetchAreaData();

  @Operation(summary = "지역 가중치 설정", description = "시군구 단위로 지역 가중치를 설정합니다. (관리자 전용)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "지역 가중치 설정 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  ResponseEntity<Void> settingAreaWeight(AreaWeightUpdateRequest request);

  @Operation(summary = "레거시 지역 코드 등록", description = "지역 코드가 재배정된 경우, 예전 코드와 현재 지역의 매핑을 등록합니다. (관리자 전용)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "레거시 지역 코드 등록 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 지역 코드")
  })
  ResponseEntity<Void> registerLegacyCodes(AreaLegacyCodeRegisterRequest request);
}
