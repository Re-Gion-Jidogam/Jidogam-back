package region.jidogam.domain.user.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.auth.entity.EmailAuthCode;
import region.jidogam.domain.auth.exception.AlreadyUsedAuthCodeException;
import region.jidogam.domain.auth.exception.EmailAuthNotFoundException;
import region.jidogam.domain.auth.exception.ExpiredEmailAuthException;
import region.jidogam.domain.auth.exception.InvalidEmailAuthException;
import region.jidogam.domain.auth.repository.EmailAuthCodeRepository;
import region.jidogam.domain.user.dto.EmailAuthRequest;
import region.jidogam.domain.user.event.EmailAuthCodeSendEvent;
import region.jidogam.domain.user.provider.EmailAuthCodeProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAuthService {

  private final EmailAuthCodeProvider emailAuthCodeProvider;
  private final EmailAuthCodeRepository emailAuthCodeRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${jidogam.email.auth.expiration}")
  private Duration expiration;

  @Transactional
  public void sendAuthCodeEmail(String email) {
    String authCode = emailAuthCodeProvider.generateAuthCode();

    emailAuthCodeRepository.findByEmail(email)
        .ifPresent(emailAuthCodeRepository::delete);

    EmailAuthCode emailAuthCode = EmailAuthCode.builder()
        .email(email)
        .code(authCode)
        .expiresAt(LocalDateTime.now().plus(expiration))
        .build();

    emailAuthCodeRepository.save(emailAuthCode);

    eventPublisher.publishEvent(EmailAuthCodeSendEvent.of(email, authCode, expiration));
    log.info("이메일 인증 코드 이벤트 발행 완료: {}", email);
  }

  @Transactional
  public void validateEmailAuthCode(EmailAuthRequest request) {
    String email = request.email();
    String authCode = request.authCode();

    EmailAuthCode emailAuthCode = emailAuthCodeRepository.findByEmail(email)
        .orElseThrow(() -> new EmailAuthNotFoundException(email));

    if (!emailAuthCode.getCode().equals(authCode)) {
      throw InvalidEmailAuthException.withCode(authCode);
    }
    if (emailAuthCode.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw ExpiredEmailAuthException.withCode(authCode);
    }
    if(emailAuthCode.getUsed()){
      throw AlreadyUsedAuthCodeException.withCode(authCode);
    }
    emailAuthCode.use();
  }
}
