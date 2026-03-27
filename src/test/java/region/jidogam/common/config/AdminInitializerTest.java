package region.jidogam.common.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.entity.User.Role;
import region.jidogam.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminInitializer 테스트")
class AdminInitializerTest {

  @InjectMocks
  private AdminInitializer adminInitializer;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private static final String ADMIN_EMAIL = "admin@jidogam.com";
  private static final String ADMIN_PASSWORD = "admin1234!";
  private static final String ADMIN_NICKNAME = "admin";

  private void setAdminFields() {
    ReflectionTestUtils.setField(adminInitializer, "adminEmail", ADMIN_EMAIL);
    ReflectionTestUtils.setField(adminInitializer, "adminPassword", ADMIN_PASSWORD);
    ReflectionTestUtils.setField(adminInitializer, "adminNickname", ADMIN_NICKNAME);
  }

  @Nested
  @DisplayName("run 메서드")
  class Run {

    @Test
    @DisplayName("관리자 계정이 없으면 새로 생성한다")
    void createsAdminWhenNotExists() {
      // given
      setAdminFields();
      when(userRepository.existsByEmail(ADMIN_EMAIL)).thenReturn(false);
      when(passwordEncoder.encode(ADMIN_PASSWORD)).thenReturn("encoded_password");

      // when
      adminInitializer.run(new DefaultApplicationArguments());

      // then
      verify(userRepository).save(argThat(user ->
          user.getEmail().equals(ADMIN_EMAIL)
              && user.getPassword().equals("encoded_password")
              && user.getNickname().equals(ADMIN_NICKNAME)
              && user.getRole() == Role.ADMIN
      ));
    }

    @Test
    @DisplayName("관리자 계정이 이미 존재하면 생성하지 않는다")
    void skipsWhenAdminAlreadyExists() {
      // given
      setAdminFields();
      when(userRepository.existsByEmail(ADMIN_EMAIL)).thenReturn(true);

      // when
      adminInitializer.run(new DefaultApplicationArguments());

      // then
      verify(userRepository, never()).save(any(User.class));
    }
  }
}
