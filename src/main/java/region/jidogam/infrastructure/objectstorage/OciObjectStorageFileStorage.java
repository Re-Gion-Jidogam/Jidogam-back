package region.jidogam.infrastructure.objectstorage;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import region.jidogam.common.entity.RetryFailureLog;
import region.jidogam.common.entity.RetryFailureLog.FailureType;
import region.jidogam.common.repository.RetryFailureLogRepository;
import region.jidogam.domain.File.dto.UploadUrlResponse;
import region.jidogam.domain.File.storage.FileStorage;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jidogam.storage.type", havingValue = "oci")
public class OciObjectStorageFileStorage implements FileStorage {

  private static final String IMAGE_FOLDER = "images";
  private static final int MAX_RETRIES = 3;

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final RetryFailureLogRepository failureRepository;

  @Value("${jidogam.storage.oci.bucket}")
  private String bucket;

  @Value("${jidogam.storage.oci.upload-expiration}")
  private Duration uploadExpiration;

  @Value("${jidogam.storage.oci.get-expiration}")
  private Duration getExpiration;

  @Override
  public String generateGetUrl(String key) {

    if (key == null || key.isBlank()) {
      return null;
    }

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(getExpiration)
        .getObjectRequest(getObjectRequest)
        .build();

    return s3Presigner.presignGetObject(presignRequest).url().toString();
  }

  @Override
  public UploadUrlResponse generateUploadUrl(String keyName) {
    log.debug("Generating OCI Object Storage presigned upload URL: keyName={}", keyName);

    String key = generateKey(keyName);

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
        .signatureDuration(uploadExpiration)
        .putObjectRequest(putObjectRequest)
        .build();

    URL url = s3Presigner.presignPutObject(presignRequest).url();

    return UploadUrlResponse.builder()
        .preSignedUrl(url.toString())
        .key(key)
        .build();
  }

  @Override
  public void deleteWithRetry(String key, String relatedEntityType, UUID relatedEntityId) {
    int retryCount = 0;
    Exception lastException = null;

    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build());

        log.info("OCI object deleted successfully: {} (attempt: {})", key, attempt);
        return;

      } catch (Exception e) {
        retryCount++;
        lastException = e;
        log.warn("OCI object delete failed (attempt {}/{}): {}", attempt, MAX_RETRIES,
            e.getMessage());

        if (attempt < MAX_RETRIES) {
          try {
            Thread.sleep(1000L * attempt);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      }
    }

    saveFailureLog(key, relatedEntityType, relatedEntityId, lastException, retryCount);
  }

  private String generateKey(String keyName) {
    return String.format("%s/%s/%s", IMAGE_FOLDER, UUID.randomUUID(), keyName);
  }

  private void saveFailureLog(String key, String entityType, UUID entityId,
      Exception exception, int retryCount) {
    try {
      RetryFailureLog failureLog = RetryFailureLog.builder()
          .failureType(FailureType.OCI_OBJECT_STORAGE_DELETE)
          .targetIdentifier(key)
          .context(Map.of(
              "bucket", bucket,
              "related_entity_type", entityType,
              "related_entity_id", entityId.toString()
          ))
          .errorMessage(exception.getMessage())
          .retryCount(retryCount)
          .maxRetryAttempts(MAX_RETRIES)
          .failedAt(LocalDateTime.now())
          .build();

      failureRepository.save(failureLog);
      log.error("All OCI delete attempts failed. Saved to failure log: {}", key);
    } catch (Exception e) {
      log.error("Failed to save failure log for OCI deletion: {}", key, e);
    }
  }
}
