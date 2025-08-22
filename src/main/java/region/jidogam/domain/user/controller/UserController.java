package region.jidogam.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.common.dto.response.ResponseDto;
import region.jidogam.common.util.CookieUtil;
import region.jidogam.infrastructure.jwt.dto.TokenPair;
import region.jidogam.infrastructure.jwt.dto.TokenResponse;
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final CookieUtil cookieUtil;

  @PostMapping
  public ResponseEntity<?> register(@RequestBody @Valid UserCreateRequest request) {

    TokenPair tokenPair = userService.create(request);
    cookieUtil.createRefreshTokenCookie(tokenPair.refreshToken());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ResponseDto.ok(new TokenResponse(tokenPair.accessToken())));
  }

  @GetMapping("/check-nickname")
  public ResponseEntity<?> checkNickname(@RequestParam("nickname") String nickname) {
    userService.validateNickname(nickname);
    return ResponseEntity.ok((ResponseDto.ok("사용 가능한 닉네임입니다.")));
  }

  @GetMapping("/check-email")
  public ResponseEntity<?> checkEmail(@RequestParam("email") String email) {
    userService.validateEmail(email);
    return ResponseEntity.ok((ResponseDto.ok("사용 가능한 이메일입니다.")));
  }
}
