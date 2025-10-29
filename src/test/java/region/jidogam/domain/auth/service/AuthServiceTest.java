package region.jidogam.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.domain.auth.AuthService;
import region.jidogam.domain.auth.dto.NewPasswordChangeRequest;
import region.jidogam.domain.auth.entity.PasswordResetToken;
import region.jidogam.domain.auth.exception.AlreadyUsedPasswordResetTokenException;
import region.jidogam.domain.auth.exception.InvalidPasswordResetTokenException;
import region.jidogam.domain.auth.repository.PasswordResetTokenRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.event.PasswordResetEmailSendEvent;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;
import region.jidogam.infrastructure.jwt.JwtProvider;
import region.jidogam.infrastructure.jwt.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private RefreshTokenService refreshTokenService;

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private AuthService authService;

  private Duration passwordResetTokenExpiration;
  private String frontendUrl;

  @BeforeEach
  void setUp() {
    passwordResetTokenExpiration = Duration.ofMinutes(15);
    frontendUrl = "http://localhost:3000";

    ReflectionTestUtils.setField(authService, "passwordResetTokenExpiration",
        passwordResetTokenExpiration);
    ReflectionTestUtils.setField(authService, "frontendUrl", frontendUrl);
  }

  @Nested
  @DisplayName("sendEmailWithPasswordResetUrl 메서드는")
  class SendEmailWithPasswordResetUrlTest {

    private String email;
    private User user;
    private String jwtToken;
    private String jti;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setUp() {
      email = "test@example.com";
      user = User.builder()
          .nickname("testUser")
          .email(email)
          .password("encodedPassword")
          .build();

      jwtToken = "test.jwt.token";
      jti = UUID.randomUUID().toString();
      expiresAt = LocalDateTime.now().plusMinutes(15);
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 UserNotFoundException을 던진다")
    void throwsUserNotFoundExceptionWhenUserDoesNotExist() {
      // given
      given(userRepository.findByEmail(email)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> authService.sendEmailWithPasswordResetUrl(email))
          .isInstanceOf(UserNotFoundException.class);

      then(jwtProvider).should(never()).generatePasswordResetToken(anyString());
      then(passwordResetTokenRepository).should(never()).findByEmail(anyString());
      then(eventPublisher).should(never()).publishEvent(any());
    }

    @Test
    @DisplayName("기존 토큰이 없으면 새로운 토큰을 생성하고 저장한다")
    void createsNewTokenWhenNoExistingToken() {
      // given
      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(jwtProvider.generatePasswordResetToken(email)).willReturn(jwtToken);
      given(jwtProvider.extractJwtId(jwtToken)).willReturn(jti);
      given(jwtProvider.extractExpirationTime(jwtToken)).willReturn(expiresAt);
      given(passwordResetTokenRepository.findByEmail(email)).willReturn(Optional.empty());

      // when
      authService.sendEmailWithPasswordResetUrl(email);

      // then
      ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(
          PasswordResetToken.class);
      then(passwordResetTokenRepository).should(times(1)).save(tokenCaptor.capture());

      PasswordResetToken savedToken = tokenCaptor.getValue();
      assertThat(savedToken.getEmail()).isEqualTo(email);
      assertThat(savedToken.getToken()).isEqualTo(jti);
      assertThat(savedToken.getExpiresAt()).isEqualTo(expiresAt);
      assertThat(savedToken.getUsed()).isFalse();
    }

    @Test
    @DisplayName("기존 토큰이 있으면 업데이트한다")
    void updatesExistingToken() {
      // given
      PasswordResetToken existingToken = PasswordResetToken.builder()
          .email(email)
          .token("old-jti")
          .expiresAt(LocalDateTime.now().minusMinutes(5))
          .used(false)
          .build();

      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(jwtProvider.generatePasswordResetToken(email)).willReturn(jwtToken);
      given(jwtProvider.extractJwtId(jwtToken)).willReturn(jti);
      given(jwtProvider.extractExpirationTime(jwtToken)).willReturn(expiresAt);
      given(passwordResetTokenRepository.findByEmail(email)).willReturn(
          Optional.of(existingToken));

      // when
      authService.sendEmailWithPasswordResetUrl(email);

      // then
      then(passwordResetTokenRepository).should(never()).save(any(PasswordResetToken.class));
      assertThat(existingToken.getToken()).isEqualTo(jti);
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 전송 이벤트를 발행한다")
    void publishesPasswordResetEmailEvent() {
      // given
      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(jwtProvider.generatePasswordResetToken(email)).willReturn(jwtToken);
      given(jwtProvider.extractJwtId(jwtToken)).willReturn(jti);
      given(jwtProvider.extractExpirationTime(jwtToken)).willReturn(expiresAt);
      given(passwordResetTokenRepository.findByEmail(email)).willReturn(Optional.empty());

      String expectedResetUrl = frontendUrl + "/password/reset?token=" + jwtToken;

      // when
      authService.sendEmailWithPasswordResetUrl(email);

      // then
      ArgumentCaptor<PasswordResetEmailSendEvent> eventCaptor = ArgumentCaptor.forClass(
          PasswordResetEmailSendEvent.class);
      then(eventPublisher).should(times(1)).publishEvent(eventCaptor.capture());

      PasswordResetEmailSendEvent event = eventCaptor.getValue();
      assertThat(event.email()).isEqualTo(email);
      assertThat(event.resetUrl()).isEqualTo(expectedResetUrl);
      assertThat(event.expiration()).isEqualTo(passwordResetTokenExpiration);
    }

    @Test
    @DisplayName("JWT 토큰을 생성하고 jti를 추출한다")
    void generatesJwtTokenAndExtractsJti() {
      // given
      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(jwtProvider.generatePasswordResetToken(email)).willReturn(jwtToken);
      given(jwtProvider.extractJwtId(jwtToken)).willReturn(jti);
      given(jwtProvider.extractExpirationTime(jwtToken)).willReturn(expiresAt);
      given(passwordResetTokenRepository.findByEmail(email)).willReturn(Optional.empty());

      // when
      authService.sendEmailWithPasswordResetUrl(email);

      // then
      then(jwtProvider).should(times(1)).generatePasswordResetToken(eq(email));
      then(jwtProvider).should(times(1)).extractJwtId(eq(jwtToken));
      then(jwtProvider).should(times(1)).extractExpirationTime(eq(jwtToken));
    }
  }

  @Nested
  @DisplayName("changePassword 메서드는")
  class ChangePasswordTest {

    private String jwtToken;
    private String jti;
    private String newPassword;
    private String email;
    private NewPasswordChangeRequest request;
    private PasswordResetToken passwordResetToken;
    private User user;

    @BeforeEach
    void setUp() {
      jwtToken = "valid.jwt.token";
      jti = UUID.randomUUID().toString();
      newPassword = "newPassword123!";
      email = "test@example.com";
      request = new NewPasswordChangeRequest(newPassword, jwtToken);

      user = User.builder()
          .nickname("testUser")
          .email(email)
          .password("oldPassword123!")
          .build();
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰이면 InvalidPasswordResetTokenException을 던진다")
    void throwsInvalidPasswordResetTokenExceptionWhenJwtTokenIsInvalid() {
      // given
      given(jwtProvider.validateToken(jwtToken)).willReturn(false);

      // when & then
      assertThatThrownBy(() -> authService.changePassword(request))
          .isInstanceOf(InvalidPasswordResetTokenException.class);

      then(jwtProvider).should(never()).extractJwtId(anyString());
      then(passwordResetTokenRepository).should(never()).findByToken(anyString());
      then(userRepository).should(never()).findByEmail(anyString());
      then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("DB에 토큰이 존재하지 않으면 InvalidPasswordResetTokenException을 던진다")
    void throwsInvalidPasswordResetTokenExceptionWhenTokenNotFoundInDatabase() {
      // given
      given(jwtProvider.validateToken(jwtToken)).willReturn(true);
      given(jwtProvider.extractJwtId(jwtToken)).willReturn(jti);
      given(passwordResetTokenRepository.findByToken(jti)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> authService.changePassword(request))
          .isInstanceOf(InvalidPasswordResetTokenException.class);

      then(passwordResetTokenRepository).should(never()).save(any(PasswordResetToken.class));
      then(userRepository).should(never()).findByEmail(anyString());
      then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("토큰이 만료되었으면 InvalidPasswordResetTokenException을 던진다")
    void throwsInvalidPasswordResetTokenExceptionWhenTokenIsExpired() {
      // given
      passwordResetToken = PasswordResetToken.builder()
          .email(email)
          .token(jti)
          .expiresAt(LocalDateTime.now().minusMinutes(5)) // 만료된 토큰
          .used(false)
          .build();

      given(jwtProvider.validateToken(jwtToken)).willReturn(true);
      given(jwtProvider.extractJwtId(jwtToken)).willReturn(jti);
      given(passwordResetTokenRepository.findByToken(jti)).willReturn(
          Optional.of(passwordResetToken));

      // when & then
      assertThatThrownBy(() -> authService.changePassword(request))
          .isInstanceOf(InvalidPasswordResetTokenException.class);

      then(passwordResetTokenRepository).should(never()).save(any(PasswordResetToken.class));
      then(userRepository).should(never()).findByEmail(anyString());
      then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("이미 사용된 토큰이면 AlreadyUsedPasswordResetTokenException을 던진다")
    void throwsAlreadyUsedPasswordResetTokenExceptionWhenTokenIsAlreadyUsed() {
      // given
      passwordResetToken = PasswordResetToken.builder()
          .email(email)
          .token(jti)
          .expiresAt(LocalDateTime.now().plusMinutes(15))
          .used(true) // 이미 사용된 토큰
          .build();

      given(jwtProvider.validateToken(jwtToken)).willReturn(true);
      given(jwtProvider.extractJwtId(jwtToken)).willReturn(jti);
      given(passwordResetTokenRepository.findByToken(jti)).willReturn(
          Optional.of(passwordResetToken));

      // when & then
      assertThatThrownBy(() -> authService.changePassword(request))
          .isInstanceOf(AlreadyUsedPasswordResetTokenException.class);

      then(passwordResetTokenRepository).should(never()).save(any(PasswordResetToken.class));
      then(userRepository).should(never()).findByEmail(anyString());
      then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("유효한 토큰이면 토큰을 사용 처리하고 비밀번호를 변경한다")
    void marksTokenAsUsedAndChangesPasswordWhenTokenIsValid() {
      // given
      passwordResetToken = PasswordResetToken.builder()
          .email(email)
          .token(jti)
          .expiresAt(LocalDateTime.now().plusMinutes(15))
          .used(false)
          .build();

      String encodedPassword = "encodedPassword123!";

      given(jwtProvider.validateToken(jwtToken)).willReturn(true);
      given(jwtProvider.extractJwtId(jwtToken)).willReturn(jti);
      given(passwordResetTokenRepository.findByToken(jti)).willReturn(
          Optional.of(passwordResetToken));
      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(passwordEncoder.encode(newPassword)).willReturn(encodedPassword);

      // when
      authService.changePassword(request);

      // then
      then(passwordResetTokenRepository).should(times(1)).save(passwordResetToken);
      assertThat(passwordResetToken.getUsed()).isTrue();
      then(userRepository).should(times(1)).findByEmail(eq(email));
      then(passwordEncoder).should(times(1)).encode(eq(newPassword));
    }

    @Test
    @DisplayName("JWT 토큰 검증과 jti 추출이 올바르게 수행된다")
    void validatesJwtTokenAndExtractsJtiCorrectly() {
      // given
      passwordResetToken = PasswordResetToken.builder()
          .email(email)
          .token(jti)
          .expiresAt(LocalDateTime.now().plusMinutes(15))
          .used(false)
          .build();

      String encodedPassword = "encodedPassword123!";

      given(jwtProvider.validateToken(jwtToken)).willReturn(true);
      given(jwtProvider.extractJwtId(jwtToken)).willReturn(jti);
      given(passwordResetTokenRepository.findByToken(jti)).willReturn(
          Optional.of(passwordResetToken));
      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(passwordEncoder.encode(newPassword)).willReturn(encodedPassword);

      // when
      authService.changePassword(request);

      // then
      then(jwtProvider).should(times(1)).validateToken(eq(jwtToken));
      then(jwtProvider).should(times(1)).extractJwtId(eq(jwtToken));
      then(passwordResetTokenRepository).should(times(1)).findByToken(eq(jti));
    }

    @Test
    @DisplayName("사용자가 존재하지 않아도 토큰은 사용 처리된다")
    void marksTokenAsUsedEvenWhenUserNotFound() {
      // given
      passwordResetToken = PasswordResetToken.builder()
          .email(email)
          .token(jti)
          .expiresAt(LocalDateTime.now().plusMinutes(15))
          .used(false)
          .build();

      given(jwtProvider.validateToken(jwtToken)).willReturn(true);
      given(jwtProvider.extractJwtId(jwtToken)).willReturn(jti);
      given(passwordResetTokenRepository.findByToken(jti)).willReturn(
          Optional.of(passwordResetToken));
      given(userRepository.findByEmail(email)).willReturn(Optional.empty());

      // when
      authService.changePassword(request);

      // then
      then(passwordResetTokenRepository).should(times(1)).save(passwordResetToken);
      assertThat(passwordResetToken.getUsed()).isTrue();
      then(userRepository).should(times(1)).findByEmail(eq(email));
      then(passwordEncoder).should(never()).encode(anyString());
    }
  }
}