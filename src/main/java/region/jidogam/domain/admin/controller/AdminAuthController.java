package region.jidogam.domain.admin.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.domain.admin.dto.AdminLoginRequest;
import region.jidogam.domain.admin.service.AdminAuthService;

@RestController
@RequestMapping("/jidogam-admin/api")
@RequiredArgsConstructor
public class AdminAuthController {

  private final AdminAuthService adminAuthService;

  @Value("${jidogam.admin.cookie.secure}")
  private boolean cookieSecure;

  @PostMapping("/login")
  public ResponseEntity<Void> login(@RequestBody AdminLoginRequest request,
      HttpServletResponse response) {
    String accessToken = adminAuthService.login(request);

    ResponseCookie cookie = ResponseCookie.from("access_token", accessToken)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .sameSite("Lax")
        .maxAge(3600) // 1시간
        .build();
    response.addHeader("Set-Cookie", cookie.toString());

    return ResponseEntity.ok().build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from("access_token", "")
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .sameSite("Lax")
        .maxAge(0)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());

    return ResponseEntity.ok().build();
  }
}
