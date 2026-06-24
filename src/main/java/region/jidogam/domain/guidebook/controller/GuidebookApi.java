package region.jidogam.domain.guidebook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import region.jidogam.common.annotation.CurrentUserId;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.domain.guidebook.dto.GuidebookAddPlaceRequest;
import region.jidogam.domain.guidebook.dto.GuidebookConditionRequest;
import region.jidogam.domain.guidebook.dto.GuidebookCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookPlaceConditionRequest;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.dto.GuidebookReviewConditionRequest;
import region.jidogam.domain.guidebook.dto.GuidebookReviewCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookReviewResponse;
import region.jidogam.domain.guidebook.dto.GuidebookReviewUpdateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookUpdateRequest;
import region.jidogam.domain.place.dto.PlaceResponse;

@Tag(name = "Guidebook", description = "가이드북 관련 API")
public interface GuidebookApi {

  @Operation(summary = "가이드북 목록 조회", description = "가이드북 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "가이드북 목록 조회 성공")
  })
  ResponseEntity<CursorPageResponseDto<GuidebookResponse>> list(
      @Valid @ModelAttribute GuidebookConditionRequest request
  );

  @Operation(summary = "인기 가이드북 목록 조회", description = "인기 있는 가이드북 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "인기 가이드북 목록 조회 성공")
  })
  ResponseEntity<List<GuidebookResponse>> popularList(
      @Parameter(description = "조회할 개수", example = "20")
      @RequestParam(required = false, defaultValue = "20") int limit
  );

  @Operation(summary = "지역 가이드북 목록 조회", description = "지역 가이드북 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "지역 가이드북 목록 조회 성공")
  })
  ResponseEntity<List<GuidebookResponse>> localList(
      @Parameter(description = "조회할 개수", example = "20")
      @RequestParam(required = false, defaultValue = "20") int limit
  );

  @Operation(summary = "가이드북 생성", description = "새로운 가이드북을 생성합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "가이드북 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  ResponseEntity<Void> create(
      @Valid @RequestBody GuidebookCreateRequest request,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 상세 조회", description = "가이드북 상세 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "가이드북 상세 조회 성공",
          content = @Content(schema = @Schema(implementation = GuidebookResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음")
  })
  ResponseEntity<GuidebookResponse> getById(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 수정", description = "가이드북 정보를 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "가이드북 수정 성공",
          content = @Content(schema = @Schema(implementation = GuidebookResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음")
  })
  ResponseEntity<GuidebookResponse> update(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody GuidebookUpdateRequest request,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 삭제", description = "가이드북을 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "가이드북 삭제 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음")
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 출판", description = "가이드북을 출판합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "출판 성공",
          content = @Content(schema = @Schema(implementation = GuidebookResponse.class))),
      @ApiResponse(responseCode = "400", description = "출판 조건을 충족하지 못함"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음")
  })
  ResponseEntity<GuidebookResponse> publish(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 출판 취소", description = "가이드북 출판을 취소합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "출판 취소 성공"),
      @ApiResponse(responseCode = "400", description = "참여자가 존재하여 출판 취소 불가"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음")
  })
  ResponseEntity<Void> unpublish(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 장소 목록 조회", description = "가이드북에 포함된 장소 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "장소 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음")
  })
  ResponseEntity<CursorPageResponseDto<PlaceResponse>> getPlaces(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Valid @ModelAttribute GuidebookPlaceConditionRequest request,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북에 장소 추가", description = "가이드북에 새로운 장소를 추가합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "장소 추가 성공",
          content = @Content(schema = @Schema(implementation = GuidebookResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음"),
      @ApiResponse(responseCode = "409", description = "이미 추가된 장소")
  })
  ResponseEntity<GuidebookResponse> addPlace(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody GuidebookAddPlaceRequest request,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북에서 장소 제거", description = "가이드북에서 장소를 제거합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "장소 제거 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "가이드북 또는 장소를 찾을 수 없음")
  })
  ResponseEntity<Void> removePlace(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Parameter(description = "장소 ID", required = true) @PathVariable UUID placeId,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 참여", description = "가이드북에 참여합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "참여 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음"),
      @ApiResponse(responseCode = "409", description = "이미 참여 중인 가이드북")
  })
  ResponseEntity<Void> addParticipant(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 참여 취소", description = "가이드북 참여를 취소합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "참여 취소 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음 또는 참여하지 않은 가이드북")
  })
  ResponseEntity<Void> cancelParticipation(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 리뷰 목록 조회", description = "가이드북 리뷰 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음")
  })
  @GetMapping("/{id}/reviews")
  ResponseEntity<CursorPageResponseDto<GuidebookReviewResponse>> getReviews(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Valid @ModelAttribute GuidebookReviewConditionRequest request
  );

  @Operation(summary = "가이드북 리뷰 상세 조회", description = "가이드북 리뷰를 상세 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "리뷰 조회 성공",
          content = @Content(schema = @Schema(implementation = GuidebookReviewResponse.class))),
      @ApiResponse(responseCode = "404", description = "가이드북 또는 리뷰를 찾을 수 없음")
  })
  @GetMapping("/{id}/reviews/{reviewId}")
  ResponseEntity<GuidebookReviewResponse> getReviewById(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Parameter(description = "리뷰 ID", required = true) @PathVariable UUID reviewId
  );

  @Operation(summary = "가이드북 리뷰 작성", description = "가이드북 리뷰를 작성합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "리뷰 생성 조건 부족"),
      @ApiResponse(responseCode = "404", description = "가이드북을 찾을 수 없음"),
      @ApiResponse(responseCode = "409", description = "이미 리뷰를 작성함 / 삭제한 리뷰가 있어 재작성 불가")
  })
  @PostMapping("/{id}/reviews")
  ResponseEntity<GuidebookReviewResponse> createReview(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody GuidebookReviewCreateRequest request,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 리뷰 수정", description = "가이드북 리뷰를 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "리뷰 작성자가 아님"),
      @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
  })
  @PatchMapping("/{id}/reviews/{reviewId}")
  ResponseEntity<GuidebookReviewResponse> updateReview(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Parameter(description = "리뷰 ID", required = true) @PathVariable UUID reviewId,
      @Valid @RequestBody GuidebookReviewUpdateRequest request,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );

  @Operation(summary = "가이드북 리뷰 삭제", description = "가이드북 리뷰를 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "리뷰 작성자가 아님"),
      @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
  })
  @DeleteMapping("/{id}/reviews/{reviewId}")
  ResponseEntity<Void> deleteReview(
      @Parameter(description = "가이드북 ID", required = true) @PathVariable UUID id,
      @Parameter(description = "리뷰 ID", required = true) @PathVariable UUID reviewId,
      @Parameter(hidden = true) @CurrentUserId UUID userId
  );
}
