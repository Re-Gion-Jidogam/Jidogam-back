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
import region.jidogam.domain.auth.dto.LoginResult;
import region.jidogam.domain.auth.dto.NewPasswordChangeRequest;
import region.jidogam.domain.auth.entity.PasswordResetToken;
import region.jidogam.domain.auth.exception.AlreadyUsedPasswordResetTokenException;
import region.jidogam.domain.auth.exception.InvalidPasswordResetTokenException;
import region.jidogam.domain.auth.repository.PasswordResetTokenRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.event.PasswordResetEmailSendEvent;
import region.jidogam.domain.user.exception.UserDeletedException;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.stamp.repository.StampRepository;
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
  private final StampRepository stampRepository;

  @Value("${jidogam.email.password-reset.expiration}")
  private Duration passwordResetTokenExpiration;

  @Value("${jidogam.email.password-reset.frontend-url}")
  private String frontendUrl;


  @Transactional
  public LoginResult login(LoginRequest request) throws AuthException {
    log.info("사용자 로그인 시도: email = {}", request.email());

    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new AuthException("이메일 또는 비밀번호가 올바르지 않습니다"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      log.warn("비밀번호 불일치: email = {}", request.email());
      throw new AuthException("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    if (user.isDeleted()) {
      log.warn("탈퇴한 사용자 로그인 시도: email = {}", request.email());
      throw UserDeletedException.withEmail(request.email());
    }

    refreshTokenService.delete(user);

    String accessToken = jwtProvider.generateAccessToken(user);
    String refreshToken = refreshTokenService.create(user).getRefreshToken();

    LocalDateTime lastStampedAt = stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(user.getId())
        .map(stamp -> stamp.getCreatedAt())
        .orElse(null);

    log.info("사용자 로그인 완료: id = {}, email = {}", user.getId(), user.getEmail());

    return LoginResult.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .lastStampedAt(lastStampedAt)
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

    // JWT 토큰 생성 (내부에 UUID(jti), 이메일, 만료시간 포함)
    String jwtToken = jwtProvider.generatePasswordResetToken(email);
    String jti = jwtProvider.extractJwtId(jwtToken); // JWT ID 추출
    LocalDateTime expiresAt = jwtProvider.extractExpirationTime(jwtToken);

    // 기존 토큰이 있으면 업데이트, 없으면 새로 생성
    // DB에는 JWT ID(jti)만 저장하여 재사용 방지
    Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByEmail(email);

    if (existingToken.isPresent()) {
      existingToken.get().updateTokenWithExpiresAt(jti, passwordResetTokenExpiration);
      log.info("기존 비밀번호 재설정 토큰 업데이트: email = {}, jti = {}", email, jti);
    } else {
      PasswordResetToken passwordResetToken = PasswordResetToken.builder()
          .email(email)
          .token(jti) // JWT ID만 저장
          .expiresAt(expiresAt)
          .used(false)
          .build();
      passwordResetTokenRepository.save(passwordResetToken);
      log.info("새로운 비밀번호 재설정 토큰 생성: email = {}, jti = {}", email, jti);
    }

    // 비밀번호 재설정 URL 생성 (전체 JWT 토큰 사용)
    String resetUrl = frontendUrl + "/password/reset?token=" + jwtToken;

    // 이메일 전송 이벤트 발행
    eventPublisher.publishEvent(
        PasswordResetEmailSendEvent.of(email, resetUrl, passwordResetTokenExpiration)
    );

    log.info("비밀번호 재설정 이메일 발송 이벤트 발행 완료: email = {}", email);
  }

  @Transactional
  public void changePassword(NewPasswordChangeRequest request) {
    log.info("비밀번호 재설정 시도");

    String jwtToken = request.authCode();

    // 토큰 검증(만료 시간까지 검증)
    if (!jwtProvider.validateToken(jwtToken)) {
      throw InvalidPasswordResetTokenException.withToken(jwtToken);
    }

    // JWT ID 추출
    String jti = jwtProvider.extractJwtId(jwtToken);
    PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(jti)
        .orElseThrow(
            () -> InvalidPasswordResetTokenException.withToken(jwtToken)
        );

    // 만료시간 한번 더 검증
    if (passwordResetToken.isExpired()) {
      throw InvalidPasswordResetTokenException.withToken(jwtToken);
    }

    // 사용 여부 판단
    if (passwordResetToken.getUsed()) {
      throw AlreadyUsedPasswordResetTokenException.withToken(jwtToken);
    }

    passwordResetToken.use();
    passwordResetTokenRepository.save(passwordResetToken);

    User user = userRepository.findByEmail(passwordResetToken.getEmail())
        .orElseThrow(
            () -> UserNotFoundException.withEmail(passwordResetToken.getEmail())
        );

    user.changePassword(passwordEncoder.encode((request.newPassword())));

    log.info("비밀번호 재설정 완료: email = {}", passwordResetToken.getEmail());
  }
}
