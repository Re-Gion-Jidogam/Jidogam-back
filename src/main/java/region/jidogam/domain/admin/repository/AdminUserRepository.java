package region.jidogam.domain.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import region.jidogam.domain.user.entity.User;

public interface AdminUserRepository {

  Page<User> searchUsers(String keyword, User.Role role, Boolean deleted, Pageable pageable);
}
