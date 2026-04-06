package region.jidogam.domain.stamp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import region.jidogam.common.annotation.CurrentUserId;
import region.jidogam.domain.stamp.dto.PlaceStampRequest;

@Tag(name = "Stamp", description = "스탬프 관련 API")
public interface StampApi {

  @Operation(summary = "스탬프 찍기", description = "특정 장소에 스탬프를 찍습니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "스탬프 찍기 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음"),
      @ApiResponse(responseCode = "409", description = "이미 스탬프를 찍은 장소")
  })
  ResponseEntity<Void> stampPlace(
      @Valid @RequestBody PlaceStampRequest request,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "스탬프 취소", description = "특정 장소의 스탬프를 취소합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "스탬프 취소 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "스탬프를 찾을 수 없음")
  })
  ResponseEntity<Void> cancelStamp(
      @Parameter(description = "장소 ID", required = true) @PathVariable UUID placeId,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );
}
