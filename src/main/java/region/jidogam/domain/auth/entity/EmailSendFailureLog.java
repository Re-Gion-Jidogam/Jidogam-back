package region.jidogam.domain.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;

@Entity
@Table(name = "email_send_failure_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EmailSendFailureLog extends BaseEntity {
  private String email;
  private String maskedAuthCode;
  private String errorMessage;
  private int retryCount;
  private LocalDateTime failedAt;
}
