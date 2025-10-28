package region.jidogam.domain.place.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.service.PlaceService;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

  private final PlaceService placeService;

  @GetMapping("/popular")
  public ResponseEntity<List<PlaceResponse>> popularList(
      @RequestParam(required = false, defaultValue = "20") int size
  ) {
    List<PlaceResponse> responses = placeService.popularList(size);
    return ResponseEntity.ok(responses);
  }
}
