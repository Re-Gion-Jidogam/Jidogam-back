package region.jidogam.domain.place.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.domain.place.dto.PlacePopularRequest;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.service.PlaceService;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

  private final PlaceService placeService;

  @GetMapping("/popular")
  public ResponseEntity<List<PlaceResponse>> popularList(
      @Valid @ModelAttribute PlacePopularRequest request
  ) {
    List<PlaceResponse> responses = placeService.popularList(request);
    return ResponseEntity.ok(responses);
  }
}
