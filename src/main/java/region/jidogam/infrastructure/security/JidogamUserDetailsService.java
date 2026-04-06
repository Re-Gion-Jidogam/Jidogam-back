package region.jidogam.infrastructure.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import region.jidogam.domain.user.entity.User.Role;
import region.jidogam.infrastructure.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
public class JidogamUserDetailsService {

  private final JwtProvider jwtProvider;

  public UserDetails loadUserByToken(String token) {
    String email = jwtProvider.extractUserEmail(token);
    Role role = jwtProvider.extractUserRole(token);
    UUID id = jwtProvider.extractUserId(token);

    return new JidogamUserDetails(id, email, role);
  }
}
