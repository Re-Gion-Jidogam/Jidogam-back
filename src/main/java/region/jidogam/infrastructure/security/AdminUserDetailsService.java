package region.jidogam.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.entity.User.Role;
import region.jidogam.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

    if (user.getRole() != Role.ADMIN) {
      throw new UsernameNotFoundException("관리자 권한이 없습니다.");
    }

    if (user.isDeleted()) {
      throw new UsernameNotFoundException("삭제된 계정입니다.");
    }

    return new JidogamUserDetails(user);
  }
}
