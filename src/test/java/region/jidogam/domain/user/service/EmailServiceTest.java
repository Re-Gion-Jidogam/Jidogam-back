package region.jidogam.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import region.jidogam.domain.auth.entity.EmailSendFailureLog;
import region.jidogam.domain.auth.repository.EmailSendFailureLogRepository;

@SpringBootTest
@EnableAutoConfiguration(exclude = MailHealthContributorAutoConfiguration.class)
class EmailServiceTest {

  @Autowired
  private EmailService emailService;

  @Autowired
  private EmailSendFailureLogRepository failureLogRepository;

  @MockitoBean
  private JavaMailSender mailSender;

  private MimeMessage mockMimeMessage;

  @BeforeEach
  void setUp() {
    failureLogRepository.deleteAll();
    mockMimeMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
  }

  @AfterEach
  void tearDown() {
    failureLogRepository.deleteAll();
  }

  @Test
  @DisplayName("이메일 전송 성공")
  void sendAuthCodeEmail_Success() throws Exception {
    // given
    String email = "test@example.com";
    String authCode = "123456";
    Duration expiration = Duration.ofMinutes(5);

    doNothing().when(mailSender).send(any(MimeMessage.class));

    // when
    emailService.sendAuthCodeEmail(email, authCode, expiration);

    // then
    verify(mailSender, times(1)).send(any(MimeMessage.class));

    // 실패 로그가 없어야 함
    List<EmailSendFailureLog> logs = failureLogRepository.findAll();
    assertThat(logs).isEmpty();
  }

  @Test
  @DisplayName("첫 시도 실패 후 재시도에서 성공")
  void sendAuthCodeEmail_RetrySuccess() throws Exception {
    // given
    String email = "retry@example.com";
    String authCode = "654321";
    Duration expiration = Duration.ofMinutes(5);

    // 첫 번째만 실패, 두 번째 성공
    doThrow(new MailSendException("일시적 오류"))
        .doNothing()
        .when(mailSender).send(any(MimeMessage.class));

    // when
    emailService.sendAuthCodeEmail(email, authCode, expiration);

    // then
    // 2번 호출됨 (1차 실패 + 2차 성공)
    verify(mailSender, times(2)).send(any(MimeMessage.class));

    // 실패 로그가 없어야 함 (최종적으로 성공)
    List<EmailSendFailureLog> logs = failureLogRepository.findAll();
    assertThat(logs).isEmpty();
  }

  @Test
  @DisplayName("이메일 전송 3회 모두 실패 시 DB에 실패 로그 저장")
  void sendAuthCodeEmail_AllRetriesFailed() throws Exception {
    // given
    String email = "fail@example.com";
    String authCode = "999999";
    Duration expiration = Duration.ofMinutes(5);

    doThrow(new MailSendException("SMTP 서버 연결 실패"))
        .when(mailSender).send(any(MimeMessage.class));

    // when & then
    assertDoesNotThrow(() ->
        emailService.sendAuthCodeEmail(email, authCode, expiration)
    );

    // 3번 시도됨
    verify(mailSender, times(3)).send(any(MimeMessage.class));

    // 실패 로그 확인
    List<EmailSendFailureLog> logs = failureLogRepository.findAll();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getEmail()).isEqualTo(email);
    assertThat(logs.get(0).getMaskedAuthCode()).isEqualTo("99****");
    assertThat(logs.get(0).getRetryCount()).isEqualTo(2); // 2번 재시도
    assertThat(logs.get(0).getErrorMessage()).contains("SMTP 서버 연결 실패");
    assertThat(logs.get(0).getFailedAt()).isNotNull();
  }

  @Test
  @DisplayName("두 번째 시도에서 성공")
  void sendAuthCodeEmail_SecondAttemptSuccess() throws Exception {
    // given
    String email = "second@example.com";
    String authCode = "111111";
    Duration expiration = Duration.ofMinutes(5);

    doThrow(new MailSendException("네트워크 오류"))
        .doNothing()
        .when(mailSender).send(any(MimeMessage.class));

    // when
    emailService.sendAuthCodeEmail(email, authCode, expiration);

    // then
    verify(mailSender, times(2)).send(any(MimeMessage.class));

    List<EmailSendFailureLog> logs = failureLogRepository.findAll();
    assertThat(logs).isEmpty();
  }

  @Test
  @DisplayName("MessagingException 발생 시 재시도 후 실패 로그 저장")
  void sendAuthCodeEmail_MessagingException() throws Exception {
    // given
    String email = "messaging@example.com";
    String authCode = "222222";
    Duration expiration = Duration.ofMinutes(5);

    doThrow(new RuntimeException("이메일 발송에 실패했습니다.", new MessagingException("메시지 생성 실패")))
        .when(mailSender).send(any(MimeMessage.class));

    // when & then
    assertDoesNotThrow(() ->
        emailService.sendAuthCodeEmail(email, authCode, expiration)
    );

    verify(mailSender, times(3)).send(any(MimeMessage.class));

    List<EmailSendFailureLog> logs = failureLogRepository.findAll();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getEmail()).isEqualTo(email);
    assertThat(logs.get(0).getMaskedAuthCode()).isEqualTo("22****");
    assertThat(logs.get(0).getFailedAt()).isNotNull();
  }

  @Test
  @DisplayName("인증 코드 마스킹 확인")
  void authCodeMasking() throws Exception {
    // given
    String email = "mask@example.com";
    String authCode = "ABCDEF";
    Duration expiration = Duration.ofMinutes(5);

    doThrow(new MailSendException("실패"))
        .when(mailSender).send(any(MimeMessage.class));

    // when & then
    assertDoesNotThrow(() ->
        emailService.sendAuthCodeEmail(email, authCode, expiration)
    );

    List<EmailSendFailureLog> logs = failureLogRepository.findAll();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getMaskedAuthCode()).isEqualTo("AB****");
  }

  @Test
  @DisplayName("짧은 인증 코드도 마스킹 처리")
  void shortAuthCodeMasking() throws Exception {
    // given
    String email = "short@example.com";
    String authCode = "12";
    Duration expiration = Duration.ofMinutes(5);

    doThrow(new MailSendException("실패"))
        .when(mailSender).send(any(MimeMessage.class));

    // when & then
    assertDoesNotThrow(() ->
        emailService.sendAuthCodeEmail(email, authCode, expiration)
    );

    List<EmailSendFailureLog> logs = failureLogRepository.findAll();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getMaskedAuthCode()).isEqualTo("****");
  }
}