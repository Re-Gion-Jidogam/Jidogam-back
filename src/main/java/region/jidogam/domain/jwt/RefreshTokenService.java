package region.jidogam.domain.jwt;

import java.time.LocalDateTime;
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
    log.info("사용자 token pair를 JwtSession으로 저장 시작: userId = {}", user.getId());

    refreshTokenRepository.findByUserId(user.getId()).ifPresent(token -> {
      log.debug("기존 JWT 세션 삭제: userId = {}", user.getId());
      refreshTokenRepository.delete(token);
    });

    String refreshTokenString = jwtProvider.generateRefreshToken(user);
    LocalDateTime expiresAt = jwtProvider.extractExpirationTime(refreshTokenString);

    RefreshToken refreshToken = RefreshToken.builder()
        .userId(user.getId())
        .refreshToken(refreshTokenString)
        .expiresAt(expiresAt)
        .build();

    refreshTokenRepository.save(refreshToken);
    log.info("사용자 token pair를 JwtSession으로 저장 완료");

    return refreshToken;
  }
}
