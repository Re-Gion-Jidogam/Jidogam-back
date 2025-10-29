package region.jidogam.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PasswordResetToken extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String email;

  /**
   * JWT 토큰의 ID (jti - JWT ID)를 저장
   * 실제 비밀번호 재설정 시 전체 JWT 토큰에서 jti를 추출하여 이 값과 비교
   * 이를 통해 토큰 재사용을 방지
   */
  @Column(nullable = false)
  private String token;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  @Builder.Default
  private Boolean used = false;

  public void use() {
    this.used = true;
  }

  public void updateTokenWithExpiresAt(String newToken, Duration duration) {
    this.token = newToken;
    this.expiresAt = LocalDateTime.now().plusMinutes(duration.toMinutes());
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }
}
