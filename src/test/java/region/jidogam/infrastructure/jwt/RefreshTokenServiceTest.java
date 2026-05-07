package region.jidogam.infrastructure.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.security.auth.message.AuthException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;
import region.jidogam.infrastructure.jwt.dto.TokenPair;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock
  private JwtProvider jwtProvider;
  @Mock
  private RefreshTokenRepository refreshTokenRepository;
  @Mock
  private UserRepository userRepository;
  @InjectMocks
  private RefreshTokenService refreshTokenService;

  @Nested
  @DisplayName("refreshToken 생성 테스트")
  class CreateRefreshTokenTest {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID userId = UUID.randomUUID();
      User user = mock(User.class);
      String refreshTokenString = "refreshToken";
      LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

      when(jwtProvider.generateRefreshToken(user)).thenReturn(refreshTokenString);
      when(jwtProvider.extractExpirationTime(refreshTokenString)).thenReturn(expiresAt);
      when(user.getId()).thenReturn(userId);

      //when
      RefreshToken refreshToken = refreshTokenService.create(user);

      //then
      assertNotNull(refreshToken);
      assertEquals(userId, refreshToken.getUserId());
      assertEquals(refreshTokenString, refreshToken.getRefreshToken());
      assertEquals(expiresAt, refreshToken.getExpiresAt());

      verify(refreshTokenRepository, times(1)).deleteByUserId(userId);
      verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("이미 발급한 토큰이 있는 경우 삭제 후 성공")
    void successWhenAlreadyExsitsRefreshToken() {
      //given
      UUID userId = UUID.randomUUID();
      User user = mock(User.class);
      String refreshTokenString = "refreshToken";
      LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

      when(refreshTokenRepository.deleteByUserId(userId)).thenReturn(1);
      when(jwtProvider.generateRefreshToken(user)).thenReturn(refreshTokenString);
      when(jwtProvider.extractExpirationTime(refreshTokenString)).thenReturn(expiresAt);
      when(user.getId()).thenReturn(userId);

      //when
      RefreshToken refreshToken = refreshTokenService.create(user);

      //then
      assertNotNull(refreshToken);
      assertEquals(userId, refreshToken.getUserId());
      assertEquals(refreshTokenString, refreshToken.getRefreshToken());
      assertEquals(expiresAt, refreshToken.getExpiresAt());

      verify(refreshTokenRepository, times(1)).deleteByUserId(userId);
      verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }
  }

  @Nested
  @DisplayName("refreshToken 삭제 테스트")
  class DeleteRefreshTokenTest {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID userId = UUID.randomUUID();
      User user = mock(User.class);

      when(user.getId()).thenReturn(userId);
      when(refreshTokenRepository.deleteByUserId(userId)).thenReturn(1);

      //when
      refreshTokenService.delete(user);

      //then
      verify(refreshTokenRepository, times(1)).deleteByUserId(userId);
    }

    @Test
    @DisplayName("토큰이 없는 경우에도 성공 처리")
    void successWhenNotExistsRefreshToken() {
      //given
      UUID userId = UUID.randomUUID();
      User user = mock(User.class);

      when(user.getId()).thenReturn(userId);

      //when
      refreshTokenService.delete(user);

      //then
      verify(refreshTokenRepository, times(1)).deleteByUserId(userId);
    }

  }

  @Nested
  @DisplayName("AccessToken 재발급 테스트")
  class RefreshAccessTokenTest {

    @Test
    @DisplayName("성공")
    void success() throws AuthException {
      //given
      UUID userId = UUID.randomUUID();
      User user = mock(User.class);
      RefreshToken mockRefreshToken = mock(RefreshToken.class);
      String refreshTokenString = "refreshToken";
      LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
      RefreshToken mockNewRefreshToken = mock(RefreshToken.class);
      String newRefreshTokenString = "newRefreshToken";
      LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(30);

      // 기존 토큰 검증을 위한 모킹
      when(jwtProvider.validateToken(refreshTokenString)).thenReturn(true);
      when(refreshTokenRepository.findByRefreshToken(refreshTokenString))
          .thenReturn(Optional.of(mockRefreshToken));
      when(mockRefreshToken.getExpiresAt()).thenReturn(expiresAt);

      // 사용자 조회를 위한 모킹
      when(jwtProvider.extractUserId(refreshTokenString)).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(user.getId()).thenReturn(userId);

      // 새 액세스 토큰 생성을 위한 모킹
      when(jwtProvider.generateAccessToken(user)).thenReturn("accessToken");

      // 새 리프레시 토큰 생성 시
      when(jwtProvider.generateRefreshToken(user)).thenReturn(newRefreshTokenString);
      when(jwtProvider.extractExpirationTime(newRefreshTokenString)).thenReturn(newExpiresAt);
      when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockNewRefreshToken);

      //when
      TokenPair tokenPair = refreshTokenService.refreshTokens(refreshTokenString);

      //then
      assertNotNull(tokenPair.accessToken());
      assertNotNull(tokenPair.refreshToken());
      assertEquals("accessToken", tokenPair.accessToken());
      assertEquals(newRefreshTokenString, tokenPair.refreshToken());
    }

    @Test
    @DisplayName("RefreshToken이 유효햐지 않은 경우")
    void failsWhenInvalidRefreshToken() {
      //given
      String refreshTokenString = "InvalidRefreshToken";

      when(jwtProvider.validateToken(refreshTokenString)).thenReturn(false);

      //when & then
      assertThrows(AuthException.class,
          () -> refreshTokenService.refreshTokens(refreshTokenString));

    }


    @Test
    @DisplayName("RefreshToken이 존재하지 않는 경우")
    void failsWhenNotExistsRefreshToken() {
      //given
      String refreshTokenString = "refreshTokenNotExists";

      when(jwtProvider.validateToken(refreshTokenString)).thenReturn(true);
      when(refreshTokenRepository.findByRefreshToken(refreshTokenString)).thenReturn(Optional.empty());

      //when & then
      assertThrows(AuthException.class,
          () -> refreshTokenService.refreshTokens(refreshTokenString));
    }

    @Test
    @DisplayName("RefreshToken이 만료된 경우")
    void failsWhenExpiredRefreshToken() {
      //given
      RefreshToken mockRefreshToken = mock(RefreshToken.class);
      String refreshTokenString = "expiredRefreshToken";
      LocalDateTime expiresAt = LocalDateTime.now().minusMinutes(5);

      when(jwtProvider.validateToken(refreshTokenString)).thenReturn(true);
      when(refreshTokenRepository.findByRefreshToken(refreshTokenString)).thenReturn(Optional.of(mockRefreshToken));
      when(mockRefreshToken.getExpiresAt()).thenReturn(expiresAt);

      //when & then
      assertThrows(AuthException.class,
          () -> refreshTokenService.refreshTokens(refreshTokenString));
    }

    @Test
    @DisplayName("RefreshToken의 userId가 존재하지 않는 userId인 경우")
    void failsWhenUserNotExists() {
      //given
      UUID userId = UUID.randomUUID();
      RefreshToken mockRefreshToken = mock(RefreshToken.class);
      String refreshTokenString = "refreshToken";
      LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

      when(jwtProvider.validateToken(refreshTokenString)).thenReturn(true);
      when(refreshTokenRepository.findByRefreshToken(refreshTokenString)).thenReturn(Optional.of(mockRefreshToken));
      when(mockRefreshToken.getExpiresAt()).thenReturn(expiresAt);

      when(jwtProvider.extractUserId(refreshTokenString)).thenReturn(userId);
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class,
          () -> refreshTokenService.refreshTokens(refreshTokenString));
    }
  }
}
