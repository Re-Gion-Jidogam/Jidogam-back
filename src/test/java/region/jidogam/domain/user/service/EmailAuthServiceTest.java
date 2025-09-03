package region.jidogam.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.domain.auth.entity.EmailAuthCode;
import region.jidogam.domain.auth.exception.AlreadyUsedAuthCodeException;
import region.jidogam.domain.auth.exception.EmailAuthNotFoundException;
import region.jidogam.domain.auth.exception.ExpiredEmailAuthException;
import region.jidogam.domain.auth.exception.InvalidEmailAuthException;
import region.jidogam.domain.auth.repository.EmailAuthCodeRepository;
import region.jidogam.domain.user.dto.EmailAuthRequest;
import region.jidogam.domain.user.event.EmailAuthCodeSendEvent;
import region.jidogam.domain.user.provider.EmailAuthCodeProvider;

@ExtendWith(MockitoExtension.class)
@DisplayName("이메일 인증 서비스 단위테스트")
public class EmailAuthServiceTest {

  @Mock
  private EmailAuthCodeProvider emailAuthCodeProvider;
  @Mock
  private EmailAuthCodeRepository emailAuthCodeRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @InjectMocks
  private EmailAuthService emailAuthService;

  @BeforeEach
  void setUp() {
    // Spring이 제공하는 ReflectionTestUtils 사용
    ReflectionTestUtils.setField(emailAuthService, "expiration", Duration.ofMinutes(5));
  }

  @Nested
  @DisplayName("이메일 인증 코드 전송")
  class SendEmailAuthCodeTest {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      String email = "test@test.com";
      String authCode = "1234";

      when(emailAuthCodeProvider.generateAuthCode()).thenReturn(authCode);

      when(emailAuthCodeRepository.findByEmail(email)).thenReturn(Optional.empty());

      //when
      emailAuthService.sendAuthCodeEmail(email);

      //then
      verify(eventPublisher, times(1)).publishEvent(any(EmailAuthCodeSendEvent.class));
    }

    @Test
    @DisplayName("같은 이메일로 인증번호 재발급 성공")
    void successWhenSameEmail() {
      //given
      String email = "test@test.com";
      String authCode = "12345";

      when(emailAuthCodeProvider.generateAuthCode()).thenReturn(authCode);

      EmailAuthCode mockEmailAuthCode = EmailAuthCode.builder()
          .email(email)
          .code("12346")
          .build();

      when(emailAuthCodeRepository.findByEmail(email)).thenReturn(
          Optional.ofNullable(mockEmailAuthCode));

      //when
      emailAuthService.sendAuthCodeEmail(email);

      //then
      verify(emailAuthCodeRepository, times(1)).delete(any(EmailAuthCode.class));
      verify(eventPublisher, times(1)).publishEvent(any(EmailAuthCodeSendEvent.class));
    }
  }

  @Nested
  @DisplayName("이메일 인증 코드 검증")
  class ValidateEmailAuthCodeTest {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      String email = "test@test.com";
      String authCode = "12345";

      EmailAuthCode mockEmailAuthCode = EmailAuthCode.builder()
          .email(email)
          .code(authCode)
          .expiresAt(LocalDateTime.now().plus(Duration.ofMinutes(5)))
          .build();

      EmailAuthRequest emailAuthRequest = new EmailAuthRequest(email, authCode);

      when(emailAuthCodeRepository.findByEmail(email)).thenReturn(
          Optional.ofNullable(mockEmailAuthCode));

      //when
      emailAuthService.validateEmailAuthCode(emailAuthRequest);

      //then
      assertEquals(true, mockEmailAuthCode.getUsed());
    }

    @Test
    @DisplayName("해당 이메일로 발급 이력이 없는 경우 실패")
    void failsWhenEmailAuthCodeNotFound() {
      //given
      String email = "test@test.com";
      String authCode = "12345";

      EmailAuthRequest emailAuthRequest = new EmailAuthRequest(email, authCode);

      when(emailAuthCodeRepository.findByEmail(email)).thenReturn(Optional.empty());

      //when & then
      assertThrows(EmailAuthNotFoundException.class,
          () -> emailAuthService.validateEmailAuthCode(emailAuthRequest));
    }

    @Test
    @DisplayName("인증 번호가 일치하지 않을 경우 실패")
    void failsWhenAuthCodeNotMatch() {
      //given
      String email = "test@test.com";
      String authCode = "12345";
      String notMatchAuthCode = "12346";

      EmailAuthCode mockEmailAuthCode = EmailAuthCode.builder()
          .email(email)
          .code(authCode)
          .expiresAt(LocalDateTime.now().plus(Duration.ofMinutes(5)))
          .build();

      EmailAuthRequest emailAuthRequest = new EmailAuthRequest(email, notMatchAuthCode);

      when(emailAuthCodeRepository.findByEmail(email)).thenReturn(
          Optional.ofNullable(mockEmailAuthCode));

      //when & then
      assertThrows(InvalidEmailAuthException.class,
          () -> emailAuthService.validateEmailAuthCode(emailAuthRequest));
    }

    @Test
    @DisplayName("만료된 인증 코드일 경우")
    void failsWhenAuthCodeExpired() {
      //given
      String email = "test@test.com";
      String authCode = "12345";

      EmailAuthCode mockEmailAuthCode = EmailAuthCode.builder()
          .email(email)
          .code(authCode)
          .expiresAt(LocalDateTime.now().minus(Duration.ofMinutes(10)))
          .build();

      EmailAuthRequest emailAuthRequest = new EmailAuthRequest(email, authCode);

      when(emailAuthCodeRepository.findByEmail(email)).thenReturn(
          Optional.ofNullable(mockEmailAuthCode));

      //when & then
      assertThrows(ExpiredEmailAuthException.class,
          () -> emailAuthService.validateEmailAuthCode(emailAuthRequest));
    }

    @Test
    @DisplayName("이미 사용된 인증 번호인 경우")
    void failsWhenAuthCodeAlreadyUsed() {
      //given
      String email = "test@test.com";
      String authCode = "12345";

      EmailAuthCode mockEmailAuthCode = EmailAuthCode.builder()
          .email(email)
          .code(authCode)
          .expiresAt(LocalDateTime.now().plus(Duration.ofMinutes(5)))
          .used(true)
          .build();

      EmailAuthRequest emailAuthRequest = new EmailAuthRequest(email, authCode);

      when(emailAuthCodeRepository.findByEmail(email)).thenReturn(
          Optional.ofNullable(mockEmailAuthCode));

      //when & then
      assertThrows(AlreadyUsedAuthCodeException.class,
          () -> emailAuthService.validateEmailAuthCode(emailAuthRequest));
    }
  }
}
