package region.jidogam.domain.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import region.jidogam.domain.auth.dto.LoginRequest;
import region.jidogam.domain.auth.dto.NewPasswordChangeRequest;
import region.jidogam.domain.auth.dto.PasswordResetRequest;
import region.jidogam.infrastructure.jwt.dto.TokenResponse;

@Tag(name = "Auth", description = "인증 관련 API")
public interface AuthApi {

  @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "로그인 성공",
          content = @Content(schema = @Schema(implementation = TokenResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  ResponseEntity<TokenResponse> login(
      @RequestBody @Valid LoginRequest request,
      HttpServletResponse response
  ) throws AuthException;

  @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "400", description = "refresh token이 없음")
  })
  ResponseEntity<String> logout(
      @Parameter(hidden = true)
      @CookieValue(value = "refresh", required = false) String refreshToken,
      HttpServletResponse response
  );

  @Operation(summary = "토큰 갱신", description = "refresh token을 사용하여 새로운 access token을 발급받습니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "토큰 갱신 성공",
          content = @Content(schema = @Schema(implementation = TokenResponse.class))),
      @ApiResponse(responseCode = "401", description = "유효하지 않은 refresh token")
  })
  ResponseEntity<TokenResponse> refresh(
      @Parameter(hidden = true)
      @CookieValue(value = "refresh", required = false) String refreshToken,
      HttpServletResponse response
  ) throws AuthException;

  @Operation(summary = "비밀번호 재설정 링크 발송", description = "비밀번호 재설정을 위한 링크를 이메일로 발송합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "비밀번호 재설정 링크 발송 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식"),
      @ApiResponse(responseCode = "404", description = "해당 이메일의 사용자를 찾을 수 없음")
  })
  ResponseEntity<Void> sendEmailWithPasswordResetUrl(
      @RequestBody @Valid PasswordResetRequest request
  );

  @Operation(summary = "비밀번호 재설정", description = "인증코드를 확인하고 새로운 비밀번호로 변경합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효하지 않은 인증코드"),
      @ApiResponse(responseCode = "410", description = "만료된 인증코드")
  })
  ResponseEntity<String> changePassword(
      @RequestBody @Valid NewPasswordChangeRequest request
  );
}
