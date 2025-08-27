package region.jidogam.domain.guidebook.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import region.jidogam.domain.guidebook.dto.GuidebookCreateRequest;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.exception.GuidebookBackgroundRequiredException;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GuidebookServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private GuidebookRepository guidebookRepository;

  @InjectMocks
  private GuidebookService guidebookService;

  @Test
  @DisplayName("가이드북 저장 성공")
  void success() {
    // given
    UUID userId = UUID.randomUUID();
    User user = mock(User.class);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    GuidebookCreateRequest request = new GuidebookCreateRequest(
      "title",
      "description",
      null,
      null,
      "url"
    );

    // when
    guidebookService.create(request, userId);

    // then
    verify(guidebookRepository).save(any(Guidebook.class));
  }

  @Test
  @DisplayName("배경 데이터가 하나도 없는 경우 예외 발생")
  void failsWhenBackgroundIsMissing() {
    // given
    UUID userId = UUID.randomUUID();
    User user = mock(User.class);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    GuidebookCreateRequest request = new GuidebookCreateRequest(
      "title",
      "description",
      null,
      null,
      null
    );

    // when & then
    assertThrows(GuidebookBackgroundRequiredException.class,
      () -> guidebookService.create(request, userId));

  }

  @Test
  @DisplayName("배경 색깔없이 이모지만 있는 경우 예외 발생")
  void failsWhenOnlyEmojiProvided() {
    // given
    UUID userId = UUID.randomUUID();
    User user = mock(User.class);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    GuidebookCreateRequest request = new GuidebookCreateRequest(
      "title",
      "description",
      "#emoji",
      null,
      null
    );

    // when & then
    assertThrows(GuidebookBackgroundRequiredException.class,
      () -> guidebookService.create(request, userId));

  }
}
