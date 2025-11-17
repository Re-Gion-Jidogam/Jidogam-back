package region.jidogam.domain.guidebook.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import region.jidogam.domain.File.storage.FileStorage;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuidebookEventListener {

  private final FileStorage fileStorage;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleImageDelete(ImageDeleteEvent event) {
    log.info("이미지 삭제 이벤트 수신: imageKey={}, entityType={}, entityId={}",
        event.imageKey(), event.entityType(), event.entityId());

    fileStorage.deleteWithRetry(event.imageKey(), event.entityType(), event.entityId());
  }
}
