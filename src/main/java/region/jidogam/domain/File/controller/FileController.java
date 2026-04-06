package region.jidogam.domain.File.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import region.jidogam.domain.File.dto.UploadUrlResponse;
import region.jidogam.domain.File.storage.FileStorage;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController implements FileApi {

  private final FileStorage fileStorage;

  @Override
  @PostMapping("/images/upload-url")
  public ResponseEntity<UploadUrlResponse> generateUploadUrl(
      @RequestParam String key
  ) {
    UploadUrlResponse response = fileStorage.generateUploadUrl(key);
    return ResponseEntity.ok(response);
  }

}
