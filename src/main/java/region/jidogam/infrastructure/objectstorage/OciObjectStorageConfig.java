package region.jidogam.infrastructure.objectstorage;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@ConditionalOnProperty(name = "jidogam.storage.type", havingValue = "oci")
public class OciObjectStorageConfig {

  @Value("${jidogam.storage.oci.namespace}")
  private String namespace;

  @Value("${jidogam.storage.oci.region}")
  private String region;

  @Value("${jidogam.storage.oci.access-key}")
  private String accessKey;

  @Value("${jidogam.storage.oci.secret-key}")
  private String secretKey;

  @Bean
  public S3Client ociObjectStorageS3Client() {
    return S3Client.builder()
        .endpointOverride(URI.create(
            String.format("https://%s.compat.objectstorage.%s.oraclecloud.com", namespace, region)))
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)))
        .serviceConfiguration(S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .build())
        .build();
  }

  @Bean
  public S3Presigner ociObjectStorageS3Presigner() {
    return S3Presigner.builder()
        .endpointOverride(URI.create(
            String.format("https://%s.compat.objectstorage.%s.oraclecloud.com", namespace, region)))
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)))
        .serviceConfiguration(S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .build())
        .build();
  }
}
