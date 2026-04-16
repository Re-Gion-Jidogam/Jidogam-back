package region.jidogam.domain.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import region.jidogam.domain.user.entity.User;

@Builder
public record AdminUserResponse(
    UUID id,
    String email,
    String nickname,
    String profileImageUrl,
    Long exp,
    User.Role role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt,
    boolean deleted
) {

  public static AdminUserResponse from(User user) {
    return AdminUserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .profileImageUrl(user.getProfileImageUrl())
        .exp(user.getExp())
        .role(user.getRole())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .deletedAt(user.getDeletedAt())
        .deleted(user.isDeleted())
        .build();
  }
}
