package region.jidogam.infrastructure.s3;

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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jidogam.storage.type", havingValue = "s3")
public class S3FileStorage implements FileStorage {

  private static final String S3_IMAGE_FOLDER_NAME = "images";
  private static final int MAX_RETRIES = 3;

  private final S3Presigner s3Presigner;
  private final S3Client s3Client;
  private final RetryFailureLogRepository failureRepository;

  @Value("${jidogam.storage.s3.bucket}")
  private String bucket;

  @Value("${jidogam.storage.s3.region}")
  private String region;

  @Value("${jidogam.storage.s3.presigned-url-expiration}")
  private Duration uploadExpiration;

  @Override
  public String generateGetUrl(String key) {
    if (key == null || key.isBlank()) {
      return null;
    }
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
  }

  @Override
  public UploadUrlResponse generateUploadUrl(String keyName) {

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

        log.info("S3 image deleted successfully: {} (attempt: {})", key, attempt);
        return;

      } catch (Exception e) {
        retryCount++;
        lastException = e;
        log.warn("S3 delete failed (attempt {}/{}): {}",
            attempt, MAX_RETRIES, e.getMessage());

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
    return String.format("%s/%s/%s",
        S3_IMAGE_FOLDER_NAME,
        UUID.randomUUID(),
        keyName);
  }

  private void saveFailureLog(String key, String entityType, UUID entityId, Exception exception,
      int retryCount) {
    try {
      RetryFailureLog failureLog = RetryFailureLog.builder()
          .failureType(FailureType.S3_DELETE)
          .targetIdentifier(key)
          .context(Map.of(
              "bucket", bucket,
              "related_entity_type", entityType,
              "related_entity_id", entityId
          ))
          .errorMessage(exception.getMessage())
          .retryCount(retryCount)
          .maxRetryAttempts(MAX_RETRIES)
          .failedAt(LocalDateTime.now())
          .build();

      failureRepository.save(failureLog);
      log.error("All S3 delete attempts failed. Saved to failure log: {}", key);
    } catch (Exception e) {
      log.error("Failed to save failure log for S3 deletion: {}", key, e);
    }
  }

}
