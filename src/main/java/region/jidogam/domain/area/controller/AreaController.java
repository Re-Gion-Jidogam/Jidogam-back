package region.jidogam.domain.area.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.domain.area.dto.AreaWeightUpdateRequest;
import region.jidogam.domain.area.service.AreaInitService;
import region.jidogam.domain.area.service.AreaService;

@RestController
@RequestMapping("/api/admin/area")
@RequiredArgsConstructor
public class AreaController implements AreaApi {

  private final AreaInitService areaInitService;
  private final AreaService areaService;

  @GetMapping("/fetch-init-data")
  public ResponseEntity<Void> fetchAreaData() {
    areaInitService.initializeAreaData();
    return ResponseEntity.ok().build();
  }

  @PostMapping("/set-weigth")
  public ResponseEntity<Void> settingAreaWeight(
      @Valid @RequestBody AreaWeightUpdateRequest request
  ) {
    areaService.updateAreaSettings(request);
    return ResponseEntity.ok().build();
  }
}
