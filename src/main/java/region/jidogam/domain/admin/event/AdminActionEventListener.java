package region.jidogam.domain.admin.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import region.jidogam.domain.admin.service.AdminActionHistoryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminActionEventListener {

  private final AdminActionHistoryService adminActionHistoryService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void handle(AdminActionEvent event) {
    try {
      adminActionHistoryService.record(event);
    } catch (Exception e) {
      log.error("관리자 활동 기록 실패: adminId={}, action={}",
          event.adminId(), event.actionType(), e);
    }
  }
}
