package region.jidogam.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.admin.entity.AdminActionHistory;
import region.jidogam.domain.admin.event.AdminActionEvent;
import region.jidogam.domain.admin.repository.AdminActionHistoryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminActionHistoryService {

  private final AdminActionHistoryRepository adminActionHistoryRepository;

  @Transactional
  public void record(AdminActionEvent event) {
    AdminActionHistory history = AdminActionHistory.builder()
        .adminId(event.adminId())
        .actionType(event.actionType())
        .targetType(event.targetType())
        .targetId(event.targetId())
        .changedFields(event.changedFields())
        .description(event.description())
        .build();

    adminActionHistoryRepository.save(history);
    log.info("관리자 활동 기록: adminId={}, action={}, target={}/{}",
        event.adminId(), event.actionType(), event.targetType(), event.targetId());
  }
}
