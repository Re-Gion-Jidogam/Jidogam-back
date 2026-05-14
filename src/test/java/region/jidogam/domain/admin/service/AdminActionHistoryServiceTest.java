package region.jidogam.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import region.jidogam.domain.admin.dto.FieldChange;
import region.jidogam.domain.admin.entity.AdminActionHistory;
import region.jidogam.domain.admin.entity.AdminActionHistory.ActionType;
import region.jidogam.domain.admin.entity.AdminActionHistory.TargetType;
import region.jidogam.domain.admin.event.AdminActionEvent;
import region.jidogam.domain.admin.repository.AdminActionHistoryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminActionHistoryService 테스트")
class AdminActionHistoryServiceTest {

  @InjectMocks
  private AdminActionHistoryService adminActionHistoryService;

  @Mock
  private AdminActionHistoryRepository adminActionHistoryRepository;

  @Test
  @DisplayName("이벤트를 기반으로 이력을 저장한다")
  void recordsHistoryFromEvent() {
    UUID adminId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    Map<String, FieldChange> changes = Map.of(
        "nickname", FieldChange.of("old", "new")
    );

    AdminActionEvent event = new AdminActionEvent(
        adminId, ActionType.USER_UPDATE, TargetType.USER, targetId, changes, null
    );

    adminActionHistoryService.record(event);

    ArgumentCaptor<AdminActionHistory> captor = ArgumentCaptor.forClass(AdminActionHistory.class);
    verify(adminActionHistoryRepository).save(captor.capture());

    AdminActionHistory saved = captor.getValue();
    assertThat(saved.getAdminId()).isEqualTo(adminId);
    assertThat(saved.getActionType()).isEqualTo(ActionType.USER_UPDATE);
    assertThat(saved.getTargetType()).isEqualTo(TargetType.USER);
    assertThat(saved.getTargetId()).isEqualTo(targetId);
    assertThat(saved.getChangedFields()).containsKey("nickname");
  }
}
