package region.jidogam.domain.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;
  private static final String EMAIL_TEMPLATE_NAME = "auth-code-email-template.html";

  // 이메일 발송
  public void sendAuthCodeEmail(String email, String authCode, Duration expiration) {
    try {
      log.info("HTML 이메일 발송 시작 - 수신자: {}", email);

      MimeMessage message = createMimeMessage(email, authCode, expiration);
      mailSender.send(message);

      log.info("HTML 이메일 발송 완료 - 수신자: {}", email);
    } catch (MessagingException | IOException e) {
      log.error("이메일 발송 실패 - 수신자: {}, 에러: {}", email, e.getMessage());
      throw new RuntimeException("이메일 발송에 실패했습니다.", e);
    }
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
