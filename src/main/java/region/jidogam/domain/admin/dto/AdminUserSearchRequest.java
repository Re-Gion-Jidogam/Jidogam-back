package region.jidogam.domain.admin.dto;

import region.jidogam.domain.user.entity.User;

public record AdminUserSearchRequest(
    String keyword,
    User.Role role,
    Boolean deleted,
    int page,
    int size
) {

  public AdminUserSearchRequest {
    if (page < 0) {
      page = 0;
    }
    if (size <= 0 || size > 100) {
      size = 20;
    }
  }

  public static AdminUserSearchRequest of(String keyword, User.Role role, Boolean deleted,
      Integer page, Integer size) {
    return new AdminUserSearchRequest(
        keyword,
        role,
        deleted,
        page == null ? 0 : page,
        size == null ? 20 : size
    );
  }
}
