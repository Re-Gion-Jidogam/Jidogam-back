package region.jidogam.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
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
import region.jidogam.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminGuidebookService 테스트")
class AdminGuidebookServiceTest {

  @InjectMocks
  private AdminGuidebookService adminGuidebookService;

  @Mock
  private GuidebookRepository guidebookRepository;

  @Mock
  private GuidebookPlaceRepository guidebookPlaceRepository;

  @Mock
  private GuidebookParticipationRepository guidebookParticipationRepository;

  @Mock
  private GuidebookAreaRatioRepository guidebookAreaRatioRepository;

  @Mock
  private GuidebookReviewRepository guidebookReviewRepository;

  @Mock
  private AdminGuidebookRepository adminGuidebookRepository;

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
      Guidebook guidebook = createGuidebook(guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      AdminGuidebookUpdateRequest request = new AdminGuidebookUpdateRequest("새 제목", "새 설명");

      AdminGuidebookResponse result = adminGuidebookService.updateGuidebook(guidebookId, request);

      assertThat(result.title()).isEqualTo("새 제목");
      assertThat(result.description()).isEqualTo("새 설명");
    }

    @Test
    @DisplayName("제목만 수정한다")
    void updatesTitleOnly() {
      UUID guidebookId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      AdminGuidebookUpdateRequest request = new AdminGuidebookUpdateRequest("새 제목", null);

      AdminGuidebookResponse result = adminGuidebookService.updateGuidebook(guidebookId, request);

      assertThat(result.title()).isEqualTo("새 제목");
      assertThat(result.description()).isEqualTo("테스트 설명");
    }

    @Test
    @DisplayName("존재하지 않는 가이드북 수정 시 예외가 발생한다")
    void throwsWhenGuidebookNotFound() {
      UUID guidebookId = UUID.randomUUID();
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.empty());

      AdminGuidebookUpdateRequest request = new AdminGuidebookUpdateRequest("새 제목", null);

      assertThatThrownBy(() -> adminGuidebookService.updateGuidebook(guidebookId, request))
          .isInstanceOf(GuidebookNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("unpublishGuidebook")
  class UnpublishGuidebook {

    @Test
    @DisplayName("출판된 가이드북을 강제 미출판한다")
    void forceUnpublishesPublishedGuidebook() {
      UUID guidebookId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId, true);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      adminGuidebookService.unpublishGuidebook(guidebookId);

      assertThat(guidebook.getIsPublished()).isFalse();
      assertThat(guidebook.getPublishedDate()).isNull();
      verify(guidebookAreaRatioRepository).deleteByGuidebook_Id(guidebookId);
    }

    @Test
    @DisplayName("이미 미출판 상태면 아무것도 하지 않는다")
    void skipsWhenAlreadyUnpublished() {
      UUID guidebookId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId, false);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      adminGuidebookService.unpublishGuidebook(guidebookId);

      verify(guidebookAreaRatioRepository, org.mockito.Mockito.never())
          .deleteByGuidebook_Id(any());
    }

    @Test
    @DisplayName("존재하지 않는 가이드북 미출판 시 예외가 발생한다")
    void throwsWhenGuidebookNotFound() {
      UUID guidebookId = UUID.randomUUID();
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminGuidebookService.unpublishGuidebook(guidebookId))
          .isInstanceOf(GuidebookNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("deleteGuidebook")
  class DeleteGuidebook {

    @Test
    @DisplayName("가이드북과 연관 데이터를 삭제한다")
    void deletesGuidebookAndRelatedData() {
      UUID guidebookId = UUID.randomUUID();
      Guidebook guidebook = createGuidebook(guidebookId);
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.of(guidebook));

      adminGuidebookService.deleteGuidebook(guidebookId);

      verify(guidebookReviewRepository).deleteByGuidebook_Id(guidebookId);
      verify(guidebookParticipationRepository).deleteByGuidebook_Id(guidebookId);
      verify(guidebookPlaceRepository).deleteByGuidebook(guidebook);
      verify(guidebookAreaRatioRepository).deleteByGuidebook_Id(guidebookId);
      verify(guidebookRepository).delete(guidebook);
    }

    @Test
    @DisplayName("존재하지 않는 가이드북 삭제 시 예외가 발생한다")
    void throwsWhenGuidebookNotFound() {
      UUID guidebookId = UUID.randomUUID();
      when(guidebookRepository.findById(guidebookId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminGuidebookService.deleteGuidebook(guidebookId))
          .isInstanceOf(GuidebookNotFoundException.class);
    }
  }
}
