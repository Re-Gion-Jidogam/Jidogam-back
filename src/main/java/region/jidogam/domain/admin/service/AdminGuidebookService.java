package region.jidogam.domain.admin.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.admin.dto.AdminGuidebookResponse;
import region.jidogam.domain.admin.dto.AdminGuidebookSearchRequest;
import region.jidogam.domain.admin.dto.AdminGuidebookUpdateRequest;
import region.jidogam.domain.admin.dto.FieldChange;
import region.jidogam.domain.admin.entity.AdminActionHistory.ActionType;
import region.jidogam.domain.admin.entity.AdminActionHistory.TargetType;
import region.jidogam.domain.admin.event.AdminActionEvent;
import region.jidogam.domain.admin.repository.AdminGuidebookRepository;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminGuidebookService {

  private final GuidebookRepository guidebookRepository;
  private final AdminGuidebookRepository adminGuidebookRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public Page<AdminGuidebookResponse> getGuidebooks(AdminGuidebookSearchRequest request) {
    PageRequest pageable = PageRequest.of(request.page(), request.size());

    return adminGuidebookRepository.searchGuidebooks(
        request.keyword(), request.isPublished(), pageable
    ).map(AdminGuidebookResponse::from);
  }

  @Transactional(readOnly = true)
  public AdminGuidebookResponse getGuidebook(UUID guidebookId) {
    Guidebook guidebook = guidebookRepository.findById(guidebookId)
        .orElseThrow(() -> GuidebookNotFoundException.withId(guidebookId));

    return AdminGuidebookResponse.from(guidebook);
  }

  @Transactional
  public AdminGuidebookResponse updateGuidebook(UUID guidebookId,
      AdminGuidebookUpdateRequest request, UUID currentAdminId) {
    Guidebook guidebook = guidebookRepository.findById(guidebookId)
        .orElseThrow(() -> GuidebookNotFoundException.withId(guidebookId));

    Map<String, FieldChange> changedFields = new HashMap<>();

    if (request.title() != null && !request.title().isBlank()
        && !request.title().equals(guidebook.getTitle())) {
      changedFields.put("title", FieldChange.of(guidebook.getTitle(), request.title()));
      guidebook.updateTitle(request.title());
    }

    if (request.description() != null
        && !request.description().equals(guidebook.getDescription())) {
      changedFields.put("description",
          FieldChange.of(guidebook.getDescription(), request.description()));
      guidebook.updateDescription(request.description());
    }

    log.info("관리자에 의해 가이드북 수정: guidebookId = {}, adminId = {}", guidebookId, currentAdminId);

    if (!changedFields.isEmpty()) {
      eventPublisher.publishEvent(AdminActionEvent.of(
          currentAdminId, ActionType.UPDATE, TargetType.GUIDEBOOK, guidebookId, changedFields
      ));
    }

    return AdminGuidebookResponse.from(guidebook);
  }

  @Transactional
  public void unpublishGuidebook(UUID guidebookId, UUID currentAdminId) {
    Guidebook guidebook = guidebookRepository.findById(guidebookId)
        .orElseThrow(() -> GuidebookNotFoundException.withId(guidebookId));

    if (Boolean.TRUE.equals(guidebook.getAdminHidden())) {
      log.warn("이미 관리자 숨김 상태인 가이드북입니다: guidebookId = {}", guidebookId);
      return;
    }

    guidebook.hideByAdmin();

    log.info("관리자에 의해 가이드북 강제 숨김: guidebookId = {}, adminId = {}", guidebookId,
        currentAdminId);

    eventPublisher.publishEvent(AdminActionEvent.of(
        currentAdminId, ActionType.HIDE, TargetType.GUIDEBOOK, guidebookId
    ));
  }

  @Transactional
  public void deleteGuidebook(UUID guidebookId, UUID currentAdminId) {
    Guidebook guidebook = guidebookRepository.findById(guidebookId)
        .orElseThrow(() -> GuidebookNotFoundException.withId(guidebookId));

    if (guidebook.getDeletedAt() != null) {
      log.warn("이미 삭제된 가이드북입니다: guidebookId = {}", guidebookId);
      return;
    }

    guidebook.softDelete();

    log.info("관리자에 의해 가이드북 삭제: guidebookId = {}, adminId = {}", guidebookId, currentAdminId);

    eventPublisher.publishEvent(AdminActionEvent.of(
        currentAdminId, ActionType.DELETE, TargetType.GUIDEBOOK, guidebookId
    ));
  }
}
