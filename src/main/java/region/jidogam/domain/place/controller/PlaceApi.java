package region.jidogam.domain.place.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import region.jidogam.common.annotation.CurrentUserId;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.domain.guidebook.dto.GuidebookConditionRequest;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.place.dto.PlaceNearByRequest;
import region.jidogam.domain.place.dto.PlacePopularRequest;
import region.jidogam.domain.place.dto.PlaceResponse;

@Tag(name = "Place", description = "장소 관련 API")
public interface PlaceApi {

  @Operation(summary = "인기 장소 목록 조회", description = "인기 있는 장소 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "인기 장소 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  ResponseEntity<List<PlaceResponse>> popularList(
      @Valid @ModelAttribute PlacePopularRequest request
  );

  @Operation(summary = "주변 장소 목록 조회", description = "현재 위치 주변의 장소 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "주변 장소 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  ResponseEntity<List<PlaceResponse>> nearbyList(
      @Valid @ModelAttribute PlaceNearByRequest request,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "장소별 가이드북 목록 조회", description = "특정 장소가 포함된 가이드북 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "가이드북 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
  })
  ResponseEntity<CursorPageResponseDto<GuidebookResponse>> listGuidebooksByPlace(
      @Parameter(description = "장소 ID", required = true) @PathVariable UUID pid,
      @Valid @ModelAttribute GuidebookConditionRequest request,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );
}
