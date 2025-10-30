package region.jidogam.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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

  public enum Role {
    USER, ADMIN
  }

  public void changeNickname(String nickname) {
    if(nickname != null) {
      this.nickname = nickname;
    }
  }

  public void changePassword(String password) {
    if(password != null) {
      this.password = password;
    }
  }

  public void changeProfileImage(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;   // null 허용
  }
}
