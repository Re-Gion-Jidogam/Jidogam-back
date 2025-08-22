package region.jidogam.domain.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import region.jidogam.domain.auth.dto.LoginRequest;
import region.jidogam.domain.user.entity.User;
import org.springframework.security.authentication.BadCredentialsException;
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
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public TokenPair login(LoginRequest request) {
    log.info("사용자 로그인 시도: email = {}", request.email());

    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      log.warn("비밀번호 불일치: email = {}", request.email());
      throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    refreshTokenService.delete(user);

    String accessToken = jwtProvider.generateAccessToken(user);
    String refreshToken = refreshTokenService.create(user).getRefreshToken();

    log.info("사용자 로그인 완료: id = {}, email = {}", user.getId(), user.getEmail());

    return TokenPair.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }
}
