package region.jidogam.domain.File.storage;

import region.jidogam.domain.File.dto.UploadUrlResponse;

public interface FileStorage {

  UploadUrlResponse generateUploadUrl(String key);

  String generateGetUrl(String key);
}
