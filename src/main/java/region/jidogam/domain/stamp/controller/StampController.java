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
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.common.annotation.CurrentUserId;
import region.jidogam.domain.stamp.dto.PlaceStampRequest;
import region.jidogam.domain.stamp.service.StampService;

@RestController
@RequestMapping("/api/stamps")
@RequiredArgsConstructor
public class StampController implements StampApi {

  private final StampService stampService;

  @Override
  @PostMapping
  public ResponseEntity<Void> stampPlace(
      @Valid @RequestBody PlaceStampRequest request,
      @CurrentUserId UUID userId
  ) {
    stampService.stampPlace(request, userId);
    return ResponseEntity.ok().build();
  }

  @Override
  @DeleteMapping("/{placeId}")
  public ResponseEntity<Void> cancelStamp(
      @PathVariable UUID placeId,
      @CurrentUserId UUID userId
  ) {
    stampService.cancelStamp(userId, placeId);
    return ResponseEntity.noContent().build();
  }
}
