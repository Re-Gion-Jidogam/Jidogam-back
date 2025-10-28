package region.jidogam.domain.auth;

import jakarta.security.auth.message.AuthException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.auth.dto.LoginRequest;
import region.jidogam.domain.auth.entity.PasswordResetToken;
import region.jidogam.domain.auth.repository.PasswordResetTokenRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.event.PasswordResetEmailSendEvent;
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
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenService refreshTokenService;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${jidogam.email.password-reset.expiration}")
  private Duration passwordResetTokenExpiration;

  @Value("${jidogam.email.password-reset.frontend-url}")
  private String frontendUrl;


  @Transactional
  public TokenPair login(LoginRequest request) throws AuthException {
    log.info("사용자 로그인 시도: email = {}", request.email());

    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new AuthException("이메일 또는 비밀번호가 올바르지 않습니다"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      log.warn("비밀번호 불일치: email = {}", request.email());
      throw new AuthException("이메일 또는 비밀번호가 올바르지 않습니다");
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

  @Transactional
  public void logout(String refreshToken) {
    log.info("사용자 로그아웃 시도");
    UUID userId = jwtProvider.extractUserId(refreshToken);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    refreshTokenService.delete(user);
    log.info("사용자 로그아웃 완료: id = {}", userId);
  }

  @Transactional
  public void sendEmailWithPasswordResetUrl(String email) {
    log.info("비밀번호 재설정 이메일 발송 시도: email = {}", email);

    // 사용자 존재 여부 확인
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> UserNotFoundException.withEmail(email));

    // 토큰 생성
    String token = UUID.randomUUID().toString();
    LocalDateTime expiresAt = LocalDateTime.now().plus(passwordResetTokenExpiration);

    // 기존 토큰이 있으면 업데이트, 없으면 새로 생성
    Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByEmail(email);

    if (existingToken.isPresent()) {
      existingToken.get().updateTokenWithExpiresAt(token, passwordResetTokenExpiration);
      log.info("기존 비밀번호 재설정 토큰 업데이트: email = {}", email);
    } else {
      PasswordResetToken passwordResetToken = PasswordResetToken.builder()
          .email(email)
          .token(token)
          .expiresAt(expiresAt)
          .used(false)
          .build();
      passwordResetTokenRepository.save(passwordResetToken);
      log.info("새로운 비밀번호 재설정 토큰 생성: email = {}", email);
    }

    // 비밀번호 재설정 URL 생성
    String resetUrl = frontendUrl + "/password/reset?token=" + token;

    // 이메일 전송 이벤트 발행
    eventPublisher.publishEvent(
        PasswordResetEmailSendEvent.of(email, resetUrl, passwordResetTokenExpiration)
    );

    log.info("비밀번호 재설정 이메일 발송 이벤트 발행 완료: email = {}", email);
  }
}
