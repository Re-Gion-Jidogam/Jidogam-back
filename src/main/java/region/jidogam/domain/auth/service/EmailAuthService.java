package region.jidogam.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import region.jidogam.domain.auth.entity.EmailAuthCode;
import region.jidogam.domain.auth.repository.EmailAuthCodeRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAuthService {

  private final JavaMailSender mailSender;
  private final EmailAuthCodeRepository emailAuthCodeRepository;

  @Value("${jidogam.email.auth.expiration}")
  private Duration expiration;

  private static final String EMAIL_TEMPLATE_NAME = "auth-code-email-template.html";

  @Transactional
  public void sendAuthCodeEmail(String email) {
    try {
      log.info("HTML 이메일 발송 시작 - 수신자: {}", email);

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(email);
      helper.setSubject("[지도감] 이메일 인증 번호");

      // HTML 템플릿 읽기
      String htmlContent = loadEmailTemplate(EMAIL_TEMPLATE_NAME);

      // 랜덤한 6자리 숫자 생성
      SecureRandom secureRandom = new SecureRandom();
      String authCode = String.format("%06d", secureRandom.nextInt(1000000));

      // email 및 인증 코드 저장
      // 이미 발급한 경우 삭제 후 새 인증코드 저장
      emailAuthCodeRepository.findByEmail(email).ifPresent(emailAuthCodeRepository::delete);
      EmailAuthCode emailAuthCode = EmailAuthCode.builder()
          .email(email)
          .code(authCode)
          .expiresAt(LocalDateTime.now().plus(expiration))
          .build();

      emailAuthCodeRepository.save(emailAuthCode);

      // 플레이스홀더 치환
      htmlContent = htmlContent.replace("{{AUTH_CODE}}", authCode);

      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("HTML 이메일 발송 완료 - 수신자: {}", email);

    } catch (MessagingException | IOException e) {
      log.error("이메일 발송 실패 - 수신자: {}, 에러: {}", email, e.getMessage());
      throw new RuntimeException("이메일 발송에 실패했습니다.", e);
    }
  }

  private String loadEmailTemplate(String templateName) throws IOException {
    ClassPathResource resource = new ClassPathResource("templates/email/" + templateName);
    return resource.getContentAsString(StandardCharsets.UTF_8);
  }
}