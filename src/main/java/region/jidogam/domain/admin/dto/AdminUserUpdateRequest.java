package region.jidogam.domain.admin.dto;

import region.jidogam.domain.user.entity.User;

public record AdminUserUpdateRequest(
    String nickname,
    User.Role role
) {

}
