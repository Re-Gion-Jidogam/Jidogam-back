package region.jidogam.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseUpdatableEntity;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseUpdatableEntity {

  @Column(nullable = false, unique = true)
  private String nickname;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String password;

  @Column(nullable = false, unique = true)
  private String email;

  @Column
  private String profileImageUrl;

  @Column(nullable = false)
  @Builder.Default
  private Long exp = 0L;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Role role = Role.USER;

  @Column(columnDefinition = "timestamp with time zone")
  private LocalDateTime deletedAt;

  public enum Role {
    USER, ADMIN
  }

  public void changeNickname(String nickname) {
      this.nickname = nickname;
  }

  public void changePassword(String password) {
      this.password = password;
  }

  public void changeProfileImage(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;   // null 허용
  }

  public void updateExp(long exp) {
    this.exp = exp;
  }

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void restore() {
    this.deletedAt = null;
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }
}
