package region.jidogam.domain.auth;

import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.common.dto.response.ResponseDto;
import region.jidogam.common.util.CookieUtil;
import region.jidogam.domain.auth.dto.LoginRequest;
import region.jidogam.domain.auth.dto.LoginResponse;
import region.jidogam.domain.auth.dto.LoginResult;
import region.jidogam.domain.auth.dto.NewPasswordChangeRequest;
import region.jidogam.domain.auth.dto.PasswordResetRequest;
import region.jidogam.infrastructure.jwt.RefreshTokenService;
import region.jidogam.infrastructure.jwt.dto.TokenPair;
import region.jidogam.infrastructure.jwt.dto.TokenResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

  private final AuthService authService;
  private final CookieUtil cookieUtil;
  private final RefreshTokenService refreshTokenService;

  @Override
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
      HttpServletResponse response)
      throws AuthException {
    LoginResult loginResult = authService.login(request);

    ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(loginResult.refreshToken());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new LoginResponse(loginResult.accessToken(), loginResult.lastStampedAt()));
  }

  @Override
  @PostMapping("/logout")
  public ResponseEntity<ResponseDto<String>> logout(
      @CookieValue(value = "refresh", required = false) String refreshToken,
      HttpServletResponse response) {
    if (refreshToken == null) {
      return ResponseEntity.badRequest()
          .body(ResponseDto.ok("refresh token이 없습니다."));
    }
    authService.logout(refreshToken);
    ResponseCookie refreshCookie = cookieUtil.deleteRefreshTokenCookie();
    response.addHeader("Set-Cookie", refreshCookie.toString());
    return ResponseEntity.noContent().build();
  }

  @Override
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(
      @CookieValue(value = "refresh", required = false) String refreshToken,
      HttpServletResponse response) throws AuthException {

    TokenPair tokenPair = refreshTokenService.refreshTokens(refreshToken);

    ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(tokenPair.refreshToken());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new TokenResponse(tokenPair.accessToken()));
  }

  @Override
  @PostMapping("/password/reset-link")
  public ResponseEntity<Void> sendEmailWithPasswordResetUrl(
      @Valid @RequestBody PasswordResetRequest request) {

    authService.sendEmailWithPasswordResetUrl(request.email());
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/password/new")
  public ResponseEntity<ResponseDto<String>> changePassword(
      @Valid @RequestBody NewPasswordChangeRequest request) {

    authService.changePassword(request);
    return ResponseEntity.ok(ResponseDto.ok("비밀번호가 재설정 되었습니다. 새로운 비밀번호로 다시 로그인해주세요."));
  }
}
