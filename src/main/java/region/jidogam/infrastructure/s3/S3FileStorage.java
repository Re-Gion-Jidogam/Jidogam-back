package region.jidogam.infrastructure.s3;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import region.jidogam.domain.File.dto.UploadUrlResponse;
import region.jidogam.domain.File.storage.FileStorage;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3FileStorage implements FileStorage {

  private final String S3_IMAGE_FOLDER_NAME = "images";

  private final S3Presigner s3Presigner;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${cloud.aws.s3.upload-expiration}")
  private Duration uploadExpiration;

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
  public String generateGetUrl(String key) {
    if (key == null || key.isBlank()) {
      return null;
    }
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
  }

  private String generateKey(String keyName) {
    return String.format("%s/%s/%s",
        S3_IMAGE_FOLDER_NAME,
        UUID.randomUUID(),
        keyName);
  }

}
