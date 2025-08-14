package region.jidogam.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.common.util.CookieUtil;
import region.jidogam.domain.jwt.TokenPair;
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<?> register(@RequestBody @Valid UserCreateRequest request) {

    TokenPair tokenPair = userService.create(request);
    CookieUtil.createRefreshTokenCookie(tokenPair.refreshToken());

    return ResponseEntity.status(HttpStatus.CREATED).body(tokenPair.accessToken());
  }

}
