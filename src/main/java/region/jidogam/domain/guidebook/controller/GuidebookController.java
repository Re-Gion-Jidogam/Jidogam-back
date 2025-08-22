package region.jidogam.domain.guidebook.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.domain.guidebook.dto.GuidebookCreateRequest;
import region.jidogam.domain.guidebook.service.GuidebookService;

@RestController
@RequestMapping("/api/guidebooks")
@RequiredArgsConstructor
public class GuidebookController {

  private final GuidebookService guidebookService;

  @PostMapping()
  public ResponseEntity<Void> create(
    @Valid @RequestBody GuidebookCreateRequest request,
    @RequestParam UUID userId // 임시
  ) {
    guidebookService.create(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
