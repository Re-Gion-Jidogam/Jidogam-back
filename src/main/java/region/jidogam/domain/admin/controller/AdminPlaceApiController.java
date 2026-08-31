package region.jidogam.domain.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.domain.place.dto.PlaceStoreInitRequest;
import region.jidogam.domain.place.dto.PlaceStoreInitResponse;
import region.jidogam.domain.place.service.PlaceInitService;

@RestController
@RequestMapping("/jidogam-admin/api/places")
@RequiredArgsConstructor
public class AdminPlaceApiController {

  private final PlaceInitService placeInitService;

  @PostMapping("/fetch-init-data")
  public ResponseEntity<PlaceStoreInitResponse> fetchStoreData(
      @Valid @RequestBody PlaceStoreInitRequest request) {
    PlaceStoreInitResponse response = placeInitService.initializeStoreData(request);
    return ResponseEntity.ok(response);
  }
}
