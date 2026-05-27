package region.jidogam.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.domain.admin.dto.AdminGuidebookResponse;
import region.jidogam.domain.admin.dto.AdminGuidebookSearchRequest;
import region.jidogam.domain.admin.dto.AdminGuidebookUpdateRequest;
import region.jidogam.domain.admin.entity.AdminActionHistory.ActionType;
import region.jidogam.domain.admin.entity.AdminActionHistory.TargetType;
import region.jidogam.domain.admin.event.AdminActionEvent;
import region.jidogam.domain.admin.repository.AdminGuidebookRepository;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminGuidebookService 테스트")
class AdminGuidebookServiceTest {

  @InjectMocks
  private AdminGuidebookService adminGuidebookService;

  @Mock
  private GuidebookRepository guidebookRepository;

  @Mock
  private AdminGuidebookRepository adminGuidebookRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  private User createUser() {
    User user = User.builder()
        .nickname("testAuthor")
        .password("password")
        .email("author@test.com")
        .build();
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }

  private Guidebook createGuidebook(UUID guidebookId) {
    return createGuidebook(guidebookId, false);
  }

  private Guidebook createGuidebook(UUID guidebookId, boolean isPublished) {
    User author = createUser();
    Guidebook guidebook = Guidebook.builder()
        .author(author)
        .title("테스트 가이드북")
        .description("테스트 설명")
        .emoji("📚")
        .color("#FF0000")
        .isPublished(isPublished)
        .build();
    if (isPublished) {
      ReflectionTestUtils.setField(guidebook, "publishedDate", LocalDateTime.now());
    }
    ReflectionTestUtils.setField(guidebook, "id", guidebookId);
    ReflectionTestUtils.setField(guidebook, "createdAt", LocalDateTime.now());
    return guidebook;
  }

  @Nested
  @DisplayName("getGuidebooks")
  class GetGuidebooks {

    @Test
    @DisplayName("검색 조건으로 가이드북 목록을 조회한다")
    void returnsPagedGuidebooks() {
      AdminGuidebookSearchRequest request = AdminGuidebookSearchRequest.of(null, null, 0, 20);
      UUID guidebookId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      Page<Guidebook> guidebookPage = new PageImpl<>(List.of(guidebook));

      when(adminGuidebookRepository.searchGuidebooks(any(), any(), any(Pageable.class)))
          .thenReturn(guidebookPage);

      Page<AdminGuidebookResponse> result = adminGuidebookService.getGuidebooks(request);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).title()).isEqualTo("테스트 가이드북");
      assertThat(result.getContent().get(0).authorNickname()).isEqualTo("testAuthor");
    }

    @Test
    @DisplayName("키워드로 필터링하여 조회한다")
    void filtersWithKeyword() {
      AdminGuidebookSearchRequest request = AdminGuidebookSearchRequest.of("테스트", null, 0, 20);
      Page<Guidebook> emptyPage = new PageImpl<>(List.of());

      when(adminGuidebookRepository.searchGuidebooks(eq("테스트"), any(), any(Pageable.class)))
          .thenReturn(emptyPage);

      Page<AdminGuidebookResponse> result = adminGuidebookService.getGuidebooks(request);

      assertThat(result.getContent()).isEmpty();
      verify(adminGuidebookRepository).searchGuidebooks(eq("테스트"), any(), any(Pageable.class));
    }
  }

  @Nested
  @DisplayName("getGuidebook")
  class GetGuidebook {

    @Test
    @DisplayName("가이드북 ID로 상세 정보를 조회한다")
    void returnsGuidebookDetail() {
      UUID guidebookId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      AdminGuidebookResponse result = adminGuidebookService.getGuidebook(guidebookId);

      assertThat(result.title()).isEqualTo("테스트 가이드북");
      assertThat(result.authorEmail()).isEqualTo("author@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 가이드북 조회 시 예외가 발생한다")
    void throwsWhenGuidebookNotFound() {
      UUID guidebookId = UUID.randomUUID();
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminGuidebookService.getGuidebook(guidebookId))
          .isInstanceOf(GuidebookNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("updateGuidebook")
  class UpdateGuidebook {

    @Test
    @DisplayName("제목과 설명을 수정한다")
    void updatesTitleAndDescription() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      AdminGuidebookUpdateRequest request = new AdminGuidebookUpdateRequest("새 제목", "새 설명");

      AdminGuidebookResponse result = adminGuidebookService.updateGuidebook(guidebookId, request,
          adminId);

      assertThat(result.title()).isEqualTo("새 제목");
      assertThat(result.description()).isEqualTo("새 설명");
    }

    @Test
    @DisplayName("제목만 수정한다")
    void updatesTitleOnly() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      AdminGuidebookUpdateRequest request = new AdminGuidebookUpdateRequest("새 제목", null);

      AdminGuidebookResponse result = adminGuidebookService.updateGuidebook(guidebookId, request,
          adminId);

      assertThat(result.title()).isEqualTo("새 제목");
      assertThat(result.description()).isEqualTo("테스트 설명");
    }

    @Test
    @DisplayName("존재하지 않는 가이드북 수정 시 예외가 발생한다")
    void throwsWhenGuidebookNotFound() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.empty());

      AdminGuidebookUpdateRequest request = new AdminGuidebookUpdateRequest("새 제목", null);

      assertThatThrownBy(
          () -> adminGuidebookService.updateGuidebook(guidebookId, request, adminId))
          .isInstanceOf(GuidebookNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("unpublishGuidebook")
  class UnpublishGuidebook {

    @Test
    @DisplayName("가이드북을 관리자 숨김 처리한다 (isPublished/지역 비율은 보존)")
    void forceHidesGuidebookByAdmin() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId, true);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      adminGuidebookService.unpublishGuidebook(guidebookId, adminId);

      assertThat(guidebook.getAdminHidden()).isTrue();
      assertThat(guidebook.getIsPublished()).isTrue();
      assertThat(guidebook.getPublishedDate()).isNotNull();
    }

    @Test
    @DisplayName("이미 관리자 숨김 상태면 아무것도 하지 않는다")
    void skipsWhenAlreadyHidden() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId, true);
      ReflectionTestUtils.setField(guidebook, "adminHidden", true);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      adminGuidebookService.unpublishGuidebook(guidebookId, adminId);

      assertThat(guidebook.getAdminHidden()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 가이드북 숨김 시 예외가 발생한다")
    void throwsWhenGuidebookNotFound() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminGuidebookService.unpublishGuidebook(guidebookId, adminId))
          .isInstanceOf(GuidebookNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("deleteGuidebook")
  class DeleteGuidebook {

    @Test
    @DisplayName("가이드북을 소프트 삭제한다 (deletedAt 설정)")
    void softDeletesGuidebook() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      adminGuidebookService.deleteGuidebook(guidebookId, adminId);

      assertThat(guidebook.getDeletedAt()).isNotNull();
      verify(guidebookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("이미 삭제된 가이드북은 아무것도 하지 않는다")
    void skipsWhenAlreadyDeleted() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      guidebook.softDelete();
      LocalDateTime originalDeletedAt = guidebook.getDeletedAt();
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      adminGuidebookService.deleteGuidebook(guidebookId, adminId);

      assertThat(guidebook.getDeletedAt()).isEqualTo(originalDeletedAt);
    }

    @Test
    @DisplayName("존재하지 않는 가이드북 삭제 시 예외가 발생한다")
    void throwsWhenGuidebookNotFound() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminGuidebookService.deleteGuidebook(guidebookId, adminId))
          .isInstanceOf(GuidebookNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("관리자 활동 기록 이벤트 발행")
  class PublishesAdminActionEvent {

    @Test
    @DisplayName("가이드북 수정 시 UPDATE 이벤트가 발행된다")
    void publishesGuidebookUpdateEvent() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      AdminGuidebookUpdateRequest request = new AdminGuidebookUpdateRequest("새 제목", "새 설명");
      adminGuidebookService.updateGuidebook(guidebookId, request, adminId);

      ArgumentCaptor<AdminActionEvent> captor = ArgumentCaptor.forClass(AdminActionEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());

      AdminActionEvent event = captor.getValue();
      assertThat(event.adminId()).isEqualTo(adminId);
      assertThat(event.actionType()).isEqualTo(ActionType.UPDATE);
      assertThat(event.targetType()).isEqualTo(TargetType.GUIDEBOOK);
      assertThat(event.targetId()).isEqualTo(guidebookId);
      assertThat(event.changedFields()).containsKeys("title", "description");
      assertThat(event.changedFields().get("title").oldValue()).isEqualTo("테스트 가이드북");
      assertThat(event.changedFields().get("title").newValue()).isEqualTo("새 제목");
    }

    @Test
    @DisplayName("변경 사항이 없으면 수정 이벤트가 발행되지 않는다")
    void doesNotPublishWhenNoUpdateChange() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      AdminGuidebookUpdateRequest request = new AdminGuidebookUpdateRequest(null, null);
      adminGuidebookService.updateGuidebook(guidebookId, request, adminId);

      verify(eventPublisher, never()).publishEvent(any(AdminActionEvent.class));
    }

    @Test
    @DisplayName("가이드북 숨김 시 HIDE 이벤트가 발행된다")
    void publishesGuidebookHideEvent() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId, true);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      adminGuidebookService.unpublishGuidebook(guidebookId, adminId);

      ArgumentCaptor<AdminActionEvent> captor = ArgumentCaptor.forClass(AdminActionEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());

      AdminActionEvent event = captor.getValue();
      assertThat(event.adminId()).isEqualTo(adminId);
      assertThat(event.actionType()).isEqualTo(ActionType.HIDE);
      assertThat(event.targetType()).isEqualTo(TargetType.GUIDEBOOK);
      assertThat(event.targetId()).isEqualTo(guidebookId);
    }

    @Test
    @DisplayName("이미 숨김 상태면 HIDE 이벤트가 발행되지 않는다")
    void doesNotPublishWhenAlreadyHidden() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId, true);
      ReflectionTestUtils.setField(guidebook, "adminHidden", true);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      adminGuidebookService.unpublishGuidebook(guidebookId, adminId);

      verify(eventPublisher, never()).publishEvent(any(AdminActionEvent.class));
    }

    @Test
    @DisplayName("가이드북 삭제 시 DELETE 이벤트가 발행된다")
    void publishesGuidebookDeleteEvent() {
      UUID guidebookId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      adminGuidebookService.deleteGuidebook(guidebookId, adminId);

      ArgumentCaptor<AdminActionEvent> captor = ArgumentCaptor.forClass(AdminActionEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());

      AdminActionEvent event = captor.getValue();
      assertThat(event.actionType()).isEqualTo(ActionType.DELETE);
      assertThat(event.targetType()).isEqualTo(TargetType.GUIDEBOOK);
      assertThat(event.targetId()).isEqualTo(guidebookId);
    }
  }
}
