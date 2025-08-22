package region.jidogam.domain.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import region.jidogam.domain.auth.dto.LoginRequest;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;
import region.jidogam.infrastructure.jwt.JwtProvider;
import region.jidogam.infrastructure.jwt.RefreshTokenService;
import region.jidogam.infrastructure.jwt.dto.TokenPair;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;

  public TokenPair login(LoginRequest request) {
    log.info("사용자 로그인 시도: email = {}", request.email());

    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> UserNotFoundException.withEmail(request.email()));

    String accessToken = jwtProvider.generateAccessToken(user);
    String refreshToken = refreshTokenService.create(user).getRefreshToken();

    log.info("사용자 로그인 완료: id = {}, email = {}", user.getId(), user.getEmail());

    return TokenPair.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }
}
