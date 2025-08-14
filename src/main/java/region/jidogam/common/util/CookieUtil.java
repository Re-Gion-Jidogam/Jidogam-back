package region.jidogam.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

  @Value("${jwt.refresh-token-expiration}")
  private Long refreshTokenExpiration;

  @Value("${app.domain:localhost}")
  private String domain;

  @Value("${app.secure-cookie:false}") // HTTPS에서만 true
  private boolean secureCookie;

  public ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from("refresh", refreshToken)
        .httpOnly(true)
        .secure(secureCookie)
        .path("/")
        .sameSite("Lax")
        .maxAge(refreshTokenExpiration / 1000) // 초 단위로 변환
        .domain(domain)
        .build();
  }
}
