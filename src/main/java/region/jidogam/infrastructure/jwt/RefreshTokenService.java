package region.jidogam.infrastructure.jwt;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final JwtProvider jwtProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public RefreshToken create(User user) {
    log.info("사용자 RefreshToken 생성 시작: userId = {}", user.getId());

    deleteExistingToken(user.getId());

    String refreshTokenString = jwtProvider.generateRefreshToken(user);
    LocalDateTime expiresAt = jwtProvider.extractExpirationTime(refreshTokenString);

    RefreshToken refreshToken = RefreshToken.builder()
        .userId(user.getId())
        .refreshToken(refreshTokenString)
        .expiresAt(expiresAt)
        .build();

    refreshTokenRepository.save(refreshToken);
    log.info("사용자 RefreshToken 생성 및 저장 완료");

    return refreshToken;
  }

  @Transactional
  public void delete(User user) {
    deleteExistingToken(user.getId());
  }

  private void deleteExistingToken(UUID userId) {
    refreshTokenRepository.findByUserId(userId).ifPresent(token -> {
      log.debug("기존 RefreshToken 삭제: userId={}", userId);
      refreshTokenRepository.delete(token);
    });
  }
}
