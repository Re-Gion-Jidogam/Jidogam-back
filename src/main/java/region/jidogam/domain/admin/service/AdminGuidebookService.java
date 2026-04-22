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

  @Transactional
  public void unpublishGuidebook(UUID guidebookId) {
    Guidebook guidebook = guidebookRepository.findById(guidebookId)
        .orElseThrow(() -> GuidebookNotFoundException.withId(guidebookId));

    if (!Boolean.TRUE.equals(guidebook.getIsPublished())) {
      log.warn("이미 미출판 상태인 가이드북입니다: guidebookId = {}", guidebookId);
      return;
    }

    guidebook.invalidateAreaRatio();
    guidebookAreaRatioRepository.deleteByGuidebook_Id(guidebookId);
    guidebook.unpublish();

    log.info("관리자에 의해 가이드북 강제 미출판: guidebookId = {}", guidebookId);
  }

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
