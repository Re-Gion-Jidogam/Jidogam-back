package region.jidogam.domain.stamp.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.domain.stamp.dto.PlaceStampRequest;
import region.jidogam.domain.stamp.service.StampService;

@RestController
@RequestMapping("/api/stamps")
@RequiredArgsConstructor
public class StampController {

  private final StampService stampService;

  @PostMapping
  public ResponseEntity<Void> stampPlace(
    @Valid @RequestBody PlaceStampRequest request,
    @RequestParam(name = "userId") UUID userId // 임시
  ) {
    stampService.stampPlace(request, userId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{placeId}")
  public ResponseEntity<Void> stampPlace(
    @PathVariable UUID placeId,
    @RequestParam(name = "userId") UUID userId // 임시
  ) {
    stampService.cancelStamp(userId, placeId);
    return ResponseEntity.noContent().build();
  }
}
