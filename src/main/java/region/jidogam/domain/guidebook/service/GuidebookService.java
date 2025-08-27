package region.jidogam.domain.guidebook.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import region.jidogam.domain.guidebook.dto.GuidebookCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.exception.GuidebookBackgroundRequiredException;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.mapper.GuidebookMapper;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.domain.stamp.repository.StampRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuidebookService {

  private final UserRepository userRepository;
  private final GuidebookRepository guidebookRepository;
  private final StampRepository stampRepository;
  private final GuidebookMapper guidebookMapper;

  public void create(GuidebookCreateRequest request, UUID userId) {
    User user = getUserOrThrow(userId);

    if (request.thumbnail() == null) {
      if (request.color() == null || request.emoji() == null) {
        throw GuidebookBackgroundRequiredException.required();
      }
    }

    Guidebook guidebook = Guidebook.builder()
      .author(user)
      .title(request.title())
      .description(request.description())
      .emoji(request.emoji())
      .color(request.color())
      .thumbnailUrl(request.thumbnail())
      .build();

    guidebookRepository.save(guidebook);
  }

  public GuidebookResponse getById(UUID id, UUID userId) {

    Guidebook guidebook = guidebookRepository.findById(id)
      .orElseThrow(() -> GuidebookNotFoundException.withId(id));

    int visitedPlaceCount = 0;
    if (userId != null) {
      visitedPlaceCount = stampRepository.countUserStampsInGuidebook(userId, guidebook.getId());
    }

    return guidebookMapper.toResponse(guidebook, visitedPlaceCount);
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> UserNotFoundException.withId(userId));
  }
}
