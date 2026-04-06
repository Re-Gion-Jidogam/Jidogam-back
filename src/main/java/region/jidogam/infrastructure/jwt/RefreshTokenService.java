package region.jidogam.infrastructure.jwt;

import jakarta.security.auth.message.AuthException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;
import region.jidogam.infrastructure.jwt.dto.TokenPair;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final JwtProvider jwtProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  @Transactional
  public RefreshToken create(User user) {
    log.debug("사용자 RefreshToken 생성 시작: userId = {}", user.getId());

    deleteExistingToken(user.getId());

    String refreshTokenString = jwtProvider.generateRefreshToken(user);
    LocalDateTime expiresAt = jwtProvider.extractExpirationTime(refreshTokenString);

    RefreshToken refreshToken = RefreshToken.builder()
        .userId(user.getId())
        .refreshToken(refreshTokenString)
        .expiresAt(expiresAt)
        .build();

    refreshTokenRepository.save(refreshToken);
    log.debug("사용자 RefreshToken 생성 및 저장 완료");

    return refreshToken;
  }

  @Transactional
  public void delete(User user) {
    deleteExistingToken(user.getId());
  }

  @Transactional
  public TokenPair refreshTokens(String refreshTokenString) throws AuthException {
    log.debug("RefreshToken으로 AccessToken 재발급 시도");

    User user = validateAndGetUser(refreshTokenString);

    // 유저 정보로 accessToken 재발급
    String accessToken = jwtProvider.generateAccessToken(user);
    RefreshToken newRefreshToken = create(user);

    log.debug("AccessToken 재발급 완료: userId = {}", user.getId());

    return TokenPair.builder()
        .accessToken(accessToken)
        .refreshToken(newRefreshToken.getRefreshToken())
        .build();
  }

  private User validateAndGetUser(String refreshTokenString) throws AuthException{
    // 토큰 유효성 검사
    if (!jwtProvider.validateToken(refreshTokenString)) {
      throw new AuthException("Refresh Token이 유효하지 않습니다.");
    }

    // 토큰 찾기
    RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenString)
        .orElseThrow(() -> new AuthException("Refresh Token이 만료되었거나 존재하지 않습니다."));

    // 토큰 만료 시
    if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new AuthException("Refresh Token이 만료되었거나 존재하지 않습니다.");
    }

    // 토큰에서 User ID 추출하여 유저 찾기
    UUID userId = jwtProvider.extractUserId(refreshTokenString);
    return userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
  }

  private void deleteExistingToken(UUID userId) {
    refreshTokenRepository.findByUserId(userId).ifPresent(token -> {
      log.debug("기존 RefreshToken 삭제: userId={}", userId);
      refreshTokenRepository.delete(token);
    });
  }
}
