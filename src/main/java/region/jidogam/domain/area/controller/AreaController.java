package region.jidogam.domain.area.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.domain.area.service.AreaInitService;

@RestController
@RequestMapping("/admin/area")
@RequiredArgsConstructor
public class AreaController {

  private final AreaInitService areaInitService;

  // only admin
  @GetMapping("/init")
  public ResponseEntity<Void> saveAreaData() {
    areaInitService.initializeAreaData();
    return ResponseEntity.ok().build();
  }
}
