package region.jidogam.domain.guidebook.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.common.annotation.CurrentUserId;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.domain.guidebook.dto.GuidebookAddPlaceRequest;
import region.jidogam.domain.guidebook.dto.GuidebookConditionRequest;
import region.jidogam.domain.guidebook.dto.GuidebookCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookPlaceConditionRequest;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.dto.GuidebookUpdateRequest;
import region.jidogam.domain.guidebook.service.GuidebookPlaceService;
import region.jidogam.domain.guidebook.service.GuidebookService;
import region.jidogam.domain.place.dto.PlaceResponse;

@RestController
@RequestMapping("/api/guidebooks")
@RequiredArgsConstructor
public class GuidebookController {

  private final GuidebookService guidebookService;
  private final GuidebookPlaceService guidebookPlaceService;

  @GetMapping
  public ResponseEntity<CursorPageResponseDto<GuidebookResponse>> list(
      @Valid @ModelAttribute GuidebookConditionRequest request
  ) {
    CursorPageResponseDto<GuidebookResponse> response = guidebookService.list(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/popular")
  public ResponseEntity<List<GuidebookResponse>> popularList(
      @RequestParam(required = false, defaultValue = "20") int limit
  ) {
    List<GuidebookResponse> response = guidebookService.popularList(limit);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/local")
  public ResponseEntity<List<GuidebookResponse>> localList(
      @RequestParam(required = false, defaultValue = "20") int limit
  ) {
    List<GuidebookResponse> response = guidebookService.localList(limit);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<Void> create(
      @Valid @RequestBody GuidebookCreateRequest request,
      @CurrentUserId UUID userId
  ) {
    guidebookService.create(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<GuidebookResponse> getById(
      @PathVariable UUID id,
      @CurrentUserId UUID userId
  ) {
    GuidebookResponse response = guidebookService.getById(id, userId);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<GuidebookResponse> update(
      @PathVariable UUID id,
      @Valid @RequestBody GuidebookUpdateRequest request,
      @CurrentUserId UUID userId
  ) {
    GuidebookResponse response = guidebookService.update(id, userId, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID id,
      @CurrentUserId UUID userId
  ) {
    guidebookService.delete(id, userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/places")
  public ResponseEntity<CursorPageResponseDto<PlaceResponse>> getPlaces(
      @PathVariable UUID id,
      @Valid @ModelAttribute GuidebookPlaceConditionRequest request,
      @CurrentUserId UUID userId
  ) {
    CursorPageResponseDto<PlaceResponse> response = guidebookPlaceService.searchByGuidebookId(
        id, userId, request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/places")
  public ResponseEntity<GuidebookResponse> addPlace(
      @PathVariable UUID id,
      @Valid @RequestBody GuidebookAddPlaceRequest request,
      @CurrentUserId UUID userId
  ) {
    GuidebookResponse response = guidebookService.addPlace(id, userId, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}/places/{placeId}")
  public ResponseEntity<Void> removePlace(
      @PathVariable UUID id,
      @PathVariable UUID placeId,
      @CurrentUserId UUID userId
  ) {
    guidebookService.removePlace(id, placeId, userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/participants")
  public ResponseEntity<Void> addParticipant(
      @PathVariable UUID id,
      @CurrentUserId UUID userId
  ) {
    guidebookService.addParticipant(id, userId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}/participants")
  public ResponseEntity<Void> cancelParticipation(
      @PathVariable UUID id,
      @CurrentUserId UUID userId
  ) {
    guidebookService.cancelParticipation(id, userId);
    return ResponseEntity.noContent().build();
  }
}
