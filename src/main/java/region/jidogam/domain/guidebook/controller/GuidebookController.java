package region.jidogam.domain.guidebook.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.common.dto.response.ResponseDto;
import region.jidogam.domain.guidebook.dto.GuidebookAddPlaceRequest;
import region.jidogam.domain.guidebook.dto.GuidebookCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.dto.GuidebookUpdateRequest;
import region.jidogam.domain.guidebook.service.GuidebookService;
import region.jidogam.infrastructure.security.JidogamUserDetails;

@RestController
@RequestMapping("/api/guidebooks")
@RequiredArgsConstructor
public class GuidebookController {

  private final GuidebookService guidebookService;

  @PostMapping
  public ResponseEntity<Void> create(
    @Valid @RequestBody GuidebookCreateRequest request,
    @AuthenticationPrincipal JidogamUserDetails principal
  ) {
    guidebookService.create(request, principal.getId());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ResponseDto<GuidebookResponse>> getById(
    @PathVariable UUID id,
    @AuthenticationPrincipal JidogamUserDetails principal
  ) {
    GuidebookResponse response = guidebookService.getById(id, principal.getId());
    return ResponseEntity.ok(ResponseDto.ok(response));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ResponseDto<GuidebookResponse>> update(
    @PathVariable UUID id,
    @Valid @RequestBody GuidebookUpdateRequest request,
    @AuthenticationPrincipal JidogamUserDetails principal
  ) {
    GuidebookResponse response = guidebookService.update(id, principal.getId(), request);
    return ResponseEntity.ok(ResponseDto.ok(response));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
    @PathVariable UUID id,
    @AuthenticationPrincipal JidogamUserDetails principal
  ) {
    guidebookService.delete(id, principal.getId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/places")
  public ResponseEntity<ResponseDto<GuidebookResponse>> addPlace(
    @PathVariable UUID id,
    @Valid @RequestBody GuidebookAddPlaceRequest request,
    @AuthenticationPrincipal JidogamUserDetails principal
  ) {
    GuidebookResponse response = guidebookService.addPlace(id, principal.getId(), request);
    return ResponseEntity.ok(ResponseDto.ok(response));
  }

  @DeleteMapping("/{id}/places/{placeId}")
  public ResponseEntity<Void> removePlace(
    @PathVariable UUID id,
    @PathVariable UUID placeId,
    @AuthenticationPrincipal JidogamUserDetails principal
  ) {
    guidebookService.removePlace(id, placeId, principal.getId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/participants")
  public ResponseEntity<Void> addParticipant(
    @PathVariable UUID id,
    @AuthenticationPrincipal JidogamUserDetails principal
  ) {
    guidebookService.addParticipant(id, principal.getId());
    return ResponseEntity.ok().build();
  }
}
