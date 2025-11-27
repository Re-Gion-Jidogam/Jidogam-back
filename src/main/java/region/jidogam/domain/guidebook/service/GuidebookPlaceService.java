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

    CursorPageResponseDto<PlaceResponse> byGuidebookOrderByDistance = placeService.guidebookPlaceList(
        guidebook.getId(),
        userId,
        request.userLat(),
        request.userLon(),
        request.filter(),
        request.cursor(),
        request.size()
    );

    return byGuidebookOrderByDistance.withTotalCount(guidebook.getTotalPlaceCount());
  }

  private Guidebook getOrThrow(UUID id) {
    return guidebookRepository.findById(id)
        .orElseThrow(() -> GuidebookNotFoundException.withId(id));
  }

}
