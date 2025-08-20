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
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<Void> register(@RequestBody @Valid UserCreateRequest request) {
    userService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/check-nickname")
  public ResponseEntity<?> checkNickname(@RequestParam("nickname") String nickname) {
    userService.validateNickname(nickname);
    return ResponseEntity.ok((ResponseDto.ok("사용 가능한 닉네임입니다.")));
  }
}
