package region.jidogam.infrastructure.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class JidogamUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public JidogamUserDetails loadUserById(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new UsernameNotFoundException("'id = " + userId + "'인 사용자를 찾을 수 없습니다."));
    return new JidogamUserDetails(user);
  }

  @Override
  public UserDetails loadUserByUsername(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(
            () -> new UsernameNotFoundException("'email = " + email + "'인 사용자를 찾을 수 없습니다."));
    return new JidogamUserDetails(user);
  }
}
