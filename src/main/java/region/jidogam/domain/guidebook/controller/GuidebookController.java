package region.jidogam.domain.guidebook.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.common.dto.response.ResponseDto;
import region.jidogam.domain.guidebook.dto.GuidebookAddPlaceRequest;
import region.jidogam.domain.guidebook.dto.GuidebookCreateRequest;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.service.GuidebookService;

@RestController
@RequestMapping("/api/guidebooks")
@RequiredArgsConstructor
public class GuidebookController {

  private final GuidebookService guidebookService;

  @PostMapping
  public ResponseEntity<Void> create(
    @Valid @RequestBody GuidebookCreateRequest request,
    @RequestParam UUID userId // 임시
  ) {
    guidebookService.create(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ResponseDto<GuidebookResponse>> getById(
    @PathVariable UUID id,
    @RequestParam(required = false) UUID userId // 임시
  ) {
    GuidebookResponse response = guidebookService.getById(id, userId);
    return ResponseEntity.ok(ResponseDto.ok(response));
  }

  @PostMapping("/{id}/places")
  public ResponseEntity<ResponseDto<GuidebookResponse>> addPlace(
    @PathVariable UUID id,
    @Valid @RequestBody GuidebookAddPlaceRequest request,
    @RequestParam UUID userId // 임시
  ) {
    GuidebookResponse response = guidebookService.addPlace(id, userId, request);
    return ResponseEntity.ok(ResponseDto.ok(response));
  }

  @DeleteMapping("/{id}/places/{placeId}")
  public ResponseEntity<Void> removePlace(
    @PathVariable UUID id,
    @PathVariable UUID placeId,
    @RequestParam(required = false) UUID userId // 임시
  ) {
    guidebookService.removePlace(id, placeId, userId);
    return ResponseEntity.noContent().build();
  }
}
