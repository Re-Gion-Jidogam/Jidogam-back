package region.jidogam.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.common.annotation.CurrentUserId;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.common.util.CookieUtil;
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
import region.jidogam.domain.user.service.EmailAuthService;
import region.jidogam.domain.user.service.UserService;
import region.jidogam.infrastructure.jwt.dto.TokenPair;
import region.jidogam.infrastructure.jwt.dto.TokenResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final EmailAuthService emailAuthService;
  private final CookieUtil cookieUtil;

  @PostMapping
  public ResponseEntity<TokenResponse> register(@RequestBody @Valid UserCreateRequest request,
      HttpServletResponse response) {

    TokenPair tokenPair = userService.create(request);
    ResponseCookie refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
        tokenPair.refreshToken());
    response.addHeader("Set-Cookie", refreshTokenCookie.toString());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new TokenResponse(tokenPair.accessToken()));
  }

  @GetMapping("/check-nickname")
  public ResponseEntity<String> checkNickname(@RequestParam("nickname") String nickname) {
    userService.validateNickname(nickname);
    return ResponseEntity.ok("사용 가능한 닉네임입니다.");
  }

  @GetMapping("/check-email")
  public ResponseEntity<String> checkEmail(@RequestParam("email") String email) {
    userService.validateEmail(email);
    return ResponseEntity.ok("사용 가능한 이메일입니다.");
  }

  @PostMapping("/auth-code")
  public ResponseEntity<Void> sendAuthCode(@RequestParam("email") String email) {
    emailAuthService.sendAuthCodeEmail(email);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/check-code")
  public ResponseEntity<Void> checkAuthCode(@RequestBody @Valid EmailAuthRequest request) {
    emailAuthService.validateEmailAuthCode(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/profile")
  public ResponseEntity<UserDto> getProfile(@CurrentUserId UUID userId) {
    UserDto userInfo = userService.getUserInfo(userId);
    return ResponseEntity.ok(userInfo);
  }

  @GetMapping("/{authorId}/guidebooks")
  public ResponseEntity<CursorPageResponseDto<GuidebookResponse>> getGuidebooks(
      @CurrentUserId UUID userId,
      @PathVariable UUID authorId,
      @Valid @ModelAttribute UserGuidebookSearchRequest request) {
    CursorPageResponseDto<GuidebookResponse> userGuidebookList = userService.getUserGuidebookList(userId, authorId, request);

    return ResponseEntity.ok(userGuidebookList);
  }

  @PatchMapping
  public ResponseEntity<UserDto> updateProfile(
      @CurrentUserId UUID userId,
      @Valid @RequestBody UserUpdateRequest request
  ){
    return ResponseEntity.ok(userService.update(userId, request));
  }

  @GetMapping("/stamps")
  public ResponseEntity<CursorPageResponseDto<PlaceResponse>> getStamps(
      @CurrentUserId UUID currentUserId,
      @Valid @ModelAttribute StampSearchRequest request) {

    return ResponseEntity.ok(userService.getUserStamps(currentUserId, request));
  }

  @GetMapping("/{userId}/participations")
  public ResponseEntity<CursorPageResponseDto<GuidebookParticipationResponse>> getParticipation(
      @CurrentUserId UUID currentUserId,
      @PathVariable UUID userId,
      @Valid @ModelAttribute GuidebookParticipationSearchRequest request) {

    return ResponseEntity.ok(userService.getUserParticipation(currentUserId, userId, request));
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(@CurrentUserId UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/restore")
  public ResponseEntity<Void> restore(@Valid @RequestBody UserRestoreRequest request) {
    userService.restore(request.email(), request.password());
    return ResponseEntity.ok().build();
  }
}
