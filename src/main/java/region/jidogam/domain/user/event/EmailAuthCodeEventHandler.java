package region.jidogam.domain.user.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import region.jidogam.domain.user.service.EmailService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAuthCodeEventHandler {

  private final EmailService emailService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 커밋 후 실행
  public void handleEmailAuthCodeSend(EmailAuthCodeSendEvent event) {
    log.info("이메일 인증 코드 전송 시작: {}", event.email());
      emailService.sendAuthCodeEmail(
          event.email(),
          event.authCode(),
          event.expiration()
      );
  }
}