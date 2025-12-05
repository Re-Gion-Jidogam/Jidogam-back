package region.jidogam.domain.guidebook.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.domain.guidebook.dto.GuidebookPlaceConditionRequest;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.exception.GuidebookNotFoundException;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.service.PlaceService;

@Service
@RequiredArgsConstructor
public class GuidebookPlaceService {

  private final GuidebookRepository guidebookRepository;
  private final PlaceService placeService;

  @Transactional(readOnly = true)
  public CursorPageResponseDto<PlaceResponse> searchByGuidebookId(
      UUID id, UUID userId, GuidebookPlaceConditionRequest request
  ) {
    Guidebook guidebook = getOrThrow(id);

    if (request.userLat() != null && request.userLon() != null) {
      return placeService.guidebookPlaceListByDistance(
          guidebook.getId(),
          userId,
          request.userLat(),
          request.userLon(),
          request.filter(),
          request.cursor(),
          request.size()
      ).withTotalCount(guidebook.getTotalPlaceCount());
    }

    return placeService.guidebookPlaceListByStamp(
        guidebook.getId(),
        userId,
        request.userLat(),
        request.userLon(),
        request.filter(),
        request.cursor(),
        request.size()
    ).withTotalCount(guidebook.getTotalPlaceCount());
  }

  private Guidebook getOrThrow(UUID id) {
    return guidebookRepository.findById(id)
        .orElseThrow(() -> GuidebookNotFoundException.withId(id));
  }

}
