package region.jidogam.domain.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import region.jidogam.domain.auth.entity.EmailSendFailureLog;
import region.jidogam.domain.auth.repository.EmailSendFailureLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private static final String EMAIL_TEMPLATE_NAME = "auth-code-email-template.html";
  private static final String PASSWORD_RESET_TEMPLATE_NAME = "password-reset-email-template.html";
  private final JavaMailSender mailSender;
  private final EmailSendFailureLogRepository emailSendFailureLogRepository;
  private final ThreadLocal<Integer> retryCountHolder = ThreadLocal.withInitial(() -> 0);

  // 이메일 발송
  @Retryable(
      retryFor = {MailException.class, RuntimeException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000, multiplier = 2)
  )
  public void sendAuthCodeEmail(String email, String authCode, Duration expiration) {
    try {
      int currentRetry = retryCountHolder.get();
      retryCountHolder.set(currentRetry + 1);

      log.info("이메일 인증 코드 전송 시도 ({}/3) - email: {}", retryCountHolder.get(), email);

      MimeMessage message = createMimeMessage(email, authCode, expiration);
      mailSender.send(message);

      log.info("이메일 인증 코드 전송 완료: {}", email);
      retryCountHolder.remove();

    } catch (MessagingException | IOException e) {
      log.error("이메일 발송 실패 ({}/3) - 수신자: {}", retryCountHolder.get(), email, e);
      throw new RuntimeException("이메일 발송에 실패했습니다.", e);
    }
  }

  // 이메일 발송 - 비밀번호 재설정
  @Retryable(
      retryFor = {MailException.class, RuntimeException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 2000, multiplier = 2)
  )
  public void sendPasswordResetEmail(String email, String resetUrl, Duration expiration) {
    try {
      int currentRetry = retryCountHolder.get();
      retryCountHolder.set(currentRetry + 1);

      log.info("비밀번호 재설정 이메일 전송 시도 ({}/3) - email: {}", retryCountHolder.get(), email);

      MimeMessage message = createPasswordResetMessage(email, resetUrl, expiration);
      mailSender.send(message);

      log.info("비밀번호 재설정 이메일 전송 완료: {}", email);
      retryCountHolder.remove();

    } catch (MessagingException | IOException e) {
      log.error("비밀번호 재설정 이메일 발송 실패 ({}/3) - 수신자: {}", retryCountHolder.get(), email, e);
      throw new RuntimeException("비밀번호 재설정 이메일 발송에 실패했습니다.", e);
    }
  }

  @Recover
  public void recover(RuntimeException e, String email, String authCode, Duration expiration) {
    log.error("이메일 전송 최종 실패 (재시도 3회 모두 실패): {}", email, e);
    saveFailureLog(email, authCode, e, retryCountHolder.get());
    retryCountHolder.remove();
  }

  private void saveFailureLog(String email, String authCode, Exception e, int retryCount) {
    EmailSendFailureLog failureLog = EmailSendFailureLog.builder()
        .email(email)
        .maskedAuthCode(maskAuthCode(authCode))
        .errorMessage(e.getMessage())
        .retryCount(retryCount - 1)
        .failedAt(LocalDateTime.now())
        .build();

    emailSendFailureLogRepository.save(failureLog);
    log.info("이메일 전송 실패 로그 저장 완료: email = {}", email);
  }

  private String maskAuthCode(String authCode) {
    if (authCode == null || authCode.length() < 4) {
      return "****";
    }
    return authCode.substring(0, 2) + "****";
  }

  // MIME 메시지 생성
  private MimeMessage createMimeMessage(String email, String authCode, Duration expiration)
      throws MessagingException, IOException {

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(email);
    helper.setSubject("[지도감] 이메일 인증 번호");

    String htmlContent = buildEmailContent(authCode, expiration);
    helper.setText(htmlContent, true);

    return message;
  }

  // MIME 메시지 생성 - 비밀번호 재설정
  private MimeMessage createPasswordResetMessage(String email, String resetUrl, Duration expiration)
      throws MessagingException, IOException {

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(email);
    helper.setSubject("[지도감] 비밀번호 재설정");

    String htmlContent = buildPasswordResetEmailContent(resetUrl, expiration);
    helper.setText(htmlContent, true);

    return message;
  }

  // 비밀번호 재설정 이메일 내용 구성
  private String buildPasswordResetEmailContent(String resetUrl, Duration expiration)
      throws IOException {
    String htmlContent = loadEmailTemplate(PASSWORD_RESET_TEMPLATE_NAME);
    htmlContent = htmlContent.replace("{{RESET_URL}}", resetUrl);
    htmlContent = htmlContent.replace("{{EXPIRATION_MINUTES}}",
        expiration.toMinutes() + "");
    return htmlContent;
  }

  // 이메일 내용 구성
  private String buildEmailContent(String authCode, Duration expiration) throws IOException {
    String htmlContent = loadEmailTemplate(EMAIL_TEMPLATE_NAME);
    htmlContent = htmlContent.replace("{{AUTH_CODE}}", authCode);
    htmlContent = htmlContent.replace("{{AUTH_CODE_EXPIRATION}}",
        expiration.toMinutes() + "");
    return htmlContent;
  }

  // 이메일 내용 템플릿 불러오기
  private String loadEmailTemplate(String templateName) throws IOException {
    ClassPathResource resource = new ClassPathResource("templates/email/" + templateName);
    return resource.getContentAsString(StandardCharsets.UTF_8);
  }
}
