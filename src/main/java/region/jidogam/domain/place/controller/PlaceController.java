package region.jidogam.domain.place.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.common.annotation.CurrentUserId;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.common.dto.response.ResponseDto;
import region.jidogam.domain.guidebook.dto.GuidebookConditionRequest;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.service.GuidebookService;
import region.jidogam.domain.place.dto.PlaceGuidebookCountRequest;
import region.jidogam.domain.place.dto.PlaceGuidebookCountResponse;
import region.jidogam.domain.place.dto.PlaceNearByRequest;
import region.jidogam.domain.place.dto.PlacePopularRequest;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.service.PlaceService;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController implements PlaceApi {

  private final PlaceService placeService;
  private final GuidebookService guidebookService;

  @Override
  @PostMapping("/guidebook-counts")
  public ResponseEntity<ResponseDto<List<PlaceGuidebookCountResponse>>> getGuidebookCounts(
      @Valid @RequestBody PlaceGuidebookCountRequest request
  ) {
    List<PlaceGuidebookCountResponse> responses = placeService.getGuidebookCounts(request);
    return ResponseEntity.ok(ResponseDto.ok(responses));
  }

  @Override
  @GetMapping("/popular")
  public ResponseEntity<List<PlaceResponse>> popularList(
      @Valid @ModelAttribute PlacePopularRequest request
  ) {
    List<PlaceResponse> responses = placeService.popularList(request);
    return ResponseEntity.ok(responses);
  }

  @Override
  @GetMapping("/nearby")
  public ResponseEntity<List<PlaceResponse>> nearbyList(
      @Valid @ModelAttribute PlaceNearByRequest request,
      @CurrentUserId UUID userId
  ) {
    List<PlaceResponse> responses = placeService.nearbyList(request, userId);
    return ResponseEntity.ok(responses);
  }

  @Override
  @GetMapping("/{pid}/guidebooks")
  public ResponseEntity<CursorPageResponseDto<GuidebookResponse>> listGuidebooksByPlace(
      @PathVariable UUID pid,
      @Valid @ModelAttribute GuidebookConditionRequest request,
      @CurrentUserId UUID userId
  ) {
    CursorPageResponseDto<GuidebookResponse> response = guidebookService.listByPlaceId(
        pid, request, userId);
    return ResponseEntity.ok(response);
  }
}
