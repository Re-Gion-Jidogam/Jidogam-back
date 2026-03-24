package region.jidogam.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "retry_failure_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RetryFailureLog extends BaseUpdatableEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private FailureType failureType;

  @Column(nullable = false, length = 500)
  private String targetIdentifier;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> context;

  @Column(length = 1000)
  private String errorMessage;

  @Builder.Default
  @Column(nullable = false)
  private Integer retryCount = 0;

  @Column(nullable = false)
  private Integer maxRetryAttempts;

  @Column(nullable = false, columnDefinition = "timestamp with time zone")
  private LocalDateTime failedAt;

  @Column(columnDefinition = "timestamp with time zone")
  private LocalDateTime lastRetryAt;

  @Column(columnDefinition = "timestamp with time zone")
  private LocalDateTime resolvedAt;

  @Getter
  @RequiredArgsConstructor
  public enum FailureType {
    EMAIL_SEND("이메일 발송"),
    S3_DELETE("S3 파일 삭제"),
    OCI_OBJECT_STORAGE_DELETE("OCI Object Storage 파일 삭제");

    private final String description;
  }
}
