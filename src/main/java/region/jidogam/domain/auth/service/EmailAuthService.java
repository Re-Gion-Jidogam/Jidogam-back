package region.jidogam.domain.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import region.jidogam.domain.auth.entity.EmailAuthCode;
import region.jidogam.domain.auth.exception.AlreadyUsedAuthCodeException;
import region.jidogam.domain.auth.exception.EmailAuthNotFoundException;
import region.jidogam.domain.auth.exception.ExpiredEmailAuthException;
import region.jidogam.domain.auth.exception.InvalidEmailAuthException;
import region.jidogam.domain.auth.repository.EmailAuthCodeRepository;
import region.jidogam.domain.user.dto.EmailAuthRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAuthService {

  private final EmailService emailService;
  private final EmailAuthCodeRepository emailAuthCodeRepository;

  @Value("${jidogam.email.auth.expiration}")
  private Duration expiration;

  public void sendAuthCodeEmail(String email) {
    String authCode = createAuthCode(email);
    //todo - 비동기 처리로 개선
    emailService.sendAuthCodeEmail(email, authCode, expiration);
  }

  // 인증 코드 생성 및 저장
  private String createAuthCode(String email) {
    String authCode = generateAuthCode();

    emailAuthCodeRepository.findByEmail(email)
        .ifPresent(emailAuthCodeRepository::delete);

    EmailAuthCode emailAuthCode = EmailAuthCode.builder()
        .email(email)
        .code(authCode)
        .expiresAt(LocalDateTime.now().plus(expiration))
        .build();

    emailAuthCodeRepository.save(emailAuthCode);
    return authCode;
  }

  private String generateAuthCode() {
    SecureRandom secureRandom = new SecureRandom();
    return String.format("%06d", secureRandom.nextInt(1000000));
  }
}
