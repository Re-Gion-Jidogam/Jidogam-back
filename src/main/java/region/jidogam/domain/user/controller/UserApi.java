package region.jidogam.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import region.jidogam.common.annotation.CurrentUserId;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.stamp.dto.StampSearchRequest;
import region.jidogam.domain.user.dto.EmailAuthRequest;
import region.jidogam.domain.user.dto.GuidebookParticipationResponse;
import region.jidogam.domain.user.dto.GuidebookParticipationSearchRequest;
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.dto.UserDto;
import region.jidogam.domain.user.dto.UserGuidebookSearchRequest;
import region.jidogam.domain.user.dto.UserRestoreRequest;
import region.jidogam.domain.user.dto.UserUpdateRequest;
import region.jidogam.infrastructure.jwt.dto.TokenResponse;

@Tag(name = "User", description = "사용자 관련 API")
public interface UserApi {

  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "회원가입 성공",
          content = @Content(schema = @Schema(implementation = TokenResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복")
  })
  ResponseEntity<TokenResponse> register(
      @RequestBody @Valid UserCreateRequest request,
      HttpServletResponse response
  );

  @Operation(summary = "닉네임 중복 확인", description = "사용 가능한 닉네임인지 확인합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임"),
      @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임")
  })
  ResponseEntity<String> checkNickname(
      @Parameter(description = "확인할 닉네임", required = true)
      @RequestParam("nickname") String nickname
  );

  @Operation(summary = "이메일 중복 확인", description = "사용 가능한 이메일인지 확인합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사용 가능한 이메일"),
      @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
  })
  ResponseEntity<String> checkEmail(
      @Parameter(description = "확인할 이메일", required = true)
      @RequestParam("email") String email
  );

  @Operation(summary = "이메일 인증코드 발송", description = "입력된 이메일로 인증코드를 발송합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "인증코드 발송 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식")
  })
  ResponseEntity<Void> sendAuthCode(
      @Parameter(description = "인증코드를 받을 이메일", required = true)
      @RequestParam("email") String email
  );

  @Operation(summary = "이메일 인증코드 확인", description = "입력된 인증코드가 유효한지 확인합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "인증코드 확인 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 인증코드")
  })
  ResponseEntity<Void> checkAuthCode(
      @RequestBody @Valid EmailAuthRequest request
  );

  @Operation(summary = "프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  ResponseEntity<UserDto> getProfile(
      @CurrentUserId UUID userId
  );

  @Operation(summary = "사용자 가이드북 목록 조회", description = "특정 사용자가 작성한 가이드북 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "가이드북 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  ResponseEntity<CursorPageResponseDto<GuidebookResponse>> getGuidebooks(
      @CurrentUserId UUID userId,
      @Parameter(description = "가이드북 작성자 ID", required = true) @PathVariable UUID authorId,
      @Valid @ModelAttribute UserGuidebookSearchRequest request
  );

  @Operation(summary = "프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "프로필 수정 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "409", description = "닉네임 중복")
  })
  ResponseEntity<UserDto> updateProfile(
      @CurrentUserId UUID userId,
      @Valid @RequestBody UserUpdateRequest request
  );

  @Operation(summary = "스탬프 목록 조회", description = "현재 로그인한 사용자의 스탬프 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "스탬프 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  ResponseEntity<CursorPageResponseDto<PlaceResponse>> getStamps(
      @CurrentUserId UUID currentUserId,
      @Valid @ModelAttribute StampSearchRequest request
  );

  @Operation(summary = "가이드북 참여 목록 조회", description = "특정 사용자가 참여 중인 가이드북 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "참여 목록 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
  })
  ResponseEntity<CursorPageResponseDto<GuidebookParticipationResponse>> getParticipation(
      @CurrentUserId UUID currentUserId,
      @Parameter(description = "조회할 사용자 ID", required = true) @PathVariable UUID userId,
      @Valid @ModelAttribute GuidebookParticipationSearchRequest request
  );

  @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  ResponseEntity<Void> delete(
      @CurrentUserId UUID userId
  );

  @Operation(summary = "계정 복구", description = "탈퇴한 계정을 복구합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "계정 복구 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "복구할 계정을 찾을 수 없음")
  })
  ResponseEntity<Void> restore(
      @Valid @RequestBody UserRestoreRequest request
  );
}