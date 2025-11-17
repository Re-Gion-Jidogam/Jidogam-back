package region.jidogam.domain.File.storage;

import java.util.UUID;
import region.jidogam.domain.File.dto.UploadUrlResponse;

public interface FileStorage {

  UploadUrlResponse generateUploadUrl(String key);

  String generateGetUrl(String key);

  void deleteWithRetry(String key, String relatedEntityType, UUID relatedEntityId);
}
