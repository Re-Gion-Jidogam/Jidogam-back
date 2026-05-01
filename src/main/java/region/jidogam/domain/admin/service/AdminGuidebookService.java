package region.jidogam.domain.admin.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.admin.dto.AdminGuidebookResponse;
import region.jidogam.domain.admin.dto.AdminGuidebookSearchRequest;
import region.jidogam.domain.admin.dto.AdminGuidebookUpdateRequest;
import region.jidogam.domain.admin.repository.AdminGuidebookRepository;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.repository.GuidebookAreaRatioRepository;
import region.jidogam.domain.guidebook.repository.GuidebookParticipationRepository;
import region.jidogam.domain.guidebook.repository.GuidebookPlaceRepository;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.domain.guidebook.repository.GuidebookReviewRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminGuidebookService {

  private final GuidebookRepository guidebookRepository;
  private final GuidebookPlaceRepository guidebookPlaceRepository;
  private final GuidebookParticipationRepository guidebookParticipationRepository;
  private final GuidebookAreaRatioRepository guidebookAreaRatioRepository;
  private final GuidebookReviewRepository guidebookReviewRepository;
  private final AdminGuidebookRepository adminGuidebookRepository;

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
      AdminGuidebookUpdateRequest request) {
    Guidebook guidebook = guidebookRepository.findById(guidebookId)
        .orElseThrow(() -> GuidebookNotFoundException.withId(guidebookId));

    if (request.title() != null && !request.title().isBlank()) {
      guidebook.updateTitle(request.title());
    }

    if (request.description() != null) {
      guidebook.updateDescription(request.description());
    }

    log.info("관리자에 의해 가이드북 수정: guidebookId = {}", guidebookId);
    return AdminGuidebookResponse.from(guidebook);
  }

  // TODO: admin_action 테이블 추가 후 관리자 액션 이력 기록 (별도 PR)
  //       - who(adminId), when(actionAt), target(guidebookId), action(HIDE), reason
  // TODO: isPublished 관련 조회 쿼리에 adminHidden = false 필터 조건 추가 (별도 작업)
  @Transactional
  public void unpublishGuidebook(UUID guidebookId) {
    Guidebook guidebook = guidebookRepository.findById(guidebookId)
        .orElseThrow(() -> GuidebookNotFoundException.withId(guidebookId));

    if (Boolean.TRUE.equals(guidebook.getAdminHidden())) {
      log.warn("이미 관리자 숨김 상태인 가이드북입니다: guidebookId = {}", guidebookId);
      return;
    }

    guidebook.hideByAdmin();

    log.info("관리자에 의해 가이드북 강제 숨김: guidebookId = {}", guidebookId);
  }

  // TODO: admin_action 테이블 추가 후 관리자 액션 이력 기록 (별도 PR)
  //       - action: DELETE
  // TODO: 소프트 삭제(deletedAt) 적용 - 사용자 측 삭제도 현재 물리 삭제이므로 함께 변경 (별도 작업)
  @Transactional
  public void deleteGuidebook(UUID guidebookId) {
    Guidebook guidebook = guidebookRepository.findById(guidebookId)
        .orElseThrow(() -> GuidebookNotFoundException.withId(guidebookId));

    guidebookReviewRepository.deleteByGuidebook_Id(guidebookId);
    guidebookParticipationRepository.deleteByGuidebook_Id(guidebookId);
    guidebookPlaceRepository.deleteByGuidebook(guidebook);
    guidebookAreaRatioRepository.deleteByGuidebook_Id(guidebookId);
    guidebookRepository.delete(guidebook);

    log.info("관리자에 의해 가이드북 삭제: guidebookId = {}", guidebookId);
  }
}
