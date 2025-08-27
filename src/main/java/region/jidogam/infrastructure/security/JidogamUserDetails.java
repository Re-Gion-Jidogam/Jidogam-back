package region.jidogam.infrastructure.security;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.entity.User.Role;

public class JidogamUserDetails implements UserDetails {

  @Getter
  private UUID id;
  private String email;
  private String password;
  private Role role;
  private Collection<? extends GrantedAuthority> authorities;

  public JidogamUserDetails(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.password = user.getPassword();
    this.role = user.getRole();
    this.authorities = getAuthorities();
  }

  public JidogamUserDetails(UUID id, String email, Role role) {
    this.id = id;
    this.email = email;
    this.role = role;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JidogamUserDetails that)) {
      return false;
    }
    return email.equals(that.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email);
  }
}