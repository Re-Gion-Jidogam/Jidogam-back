package region.jidogam.domain.auth;

import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.common.util.CookieUtil;
import region.jidogam.domain.auth.dto.LoginRequest;
import region.jidogam.infrastructure.jwt.RefreshTokenService;
import region.jidogam.infrastructure.jwt.dto.TokenPair;
import region.jidogam.infrastructure.jwt.dto.TokenResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final CookieUtil cookieUtil;
  private final RefreshTokenService refreshTokenService;

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request, HttpServletResponse response)
    throws AuthException {
    TokenPair tokenPair = authService.login(request);

    ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(tokenPair.refreshToken());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    return ResponseEntity.status(HttpStatus.CREATED)
      .body(new TokenResponse(tokenPair.accessToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(
    @CookieValue(value = "refresh", required = false) String refreshToken,
    HttpServletResponse response) {
    if (refreshToken == null) {
      return ResponseEntity.badRequest().body("refresh token이 없습니다.");
    }
    authService.logout(refreshToken);
    ResponseCookie refreshCookie = cookieUtil.deleteRefreshTokenCookie();
    response.addHeader("Set-Cookie", refreshCookie.toString());
    return ResponseEntity.noContent().build();
  }

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
}
