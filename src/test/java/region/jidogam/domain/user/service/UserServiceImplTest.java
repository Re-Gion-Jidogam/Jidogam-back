package region.jidogam.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserEmailConflictException;
import region.jidogam.domain.user.exception.UserNicknameConflictException;
import region.jidogam.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 서비스 단위테스트")
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServiceImpl userService;

  @Nested
  @DisplayName("회원가입")
  class CreateUserTest {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      String nickname = "테스트유저";
      String email = "test@email.com";
      String password = "password1234";

      UserCreateRequest userCreateRequest = new UserCreateRequest(nickname, email, password);

      when(userRepository.existsByNickname(nickname)).thenReturn(false);
      when(userRepository.existsByEmail(email)).thenReturn(false);

      when(passwordEncoder.encode("password1234")).thenReturn("encordedPassword1234");

      //when
      userService.create(userCreateRequest);

      //then
      verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("닉네임 중복이면 회원가입 실패")
    void failsWhenNicknameConflicted() {
      //given
      String nickname = "중복닉네임";
      String email = "test@email.com";
      String password = "password1234";

      UserCreateRequest userCreateRequest = new UserCreateRequest(nickname, email, password);

      when(userRepository.existsByNickname(nickname)).thenReturn(true);

      //when & then
      assertThrows(UserNicknameConflictException.class,
          () -> userService.create(userCreateRequest));

      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("이메일 중복이면 회원가입 실패")
    void failsWhenEmailConflicted() {
      //given
      String nickname = "테스트유저";
      String email = "conflicted@email.com";
      String password = "password1234";

      UserCreateRequest userCreateRequest = new UserCreateRequest(nickname, email, password);

      when(userRepository.existsByNickname(nickname)).thenReturn(false);
      when(userRepository.existsByEmail(email)).thenReturn(true);

      //when & then
      assertThrows(UserEmailConflictException.class,
          () -> userService.create(userCreateRequest));

      verify(userRepository, never()).save(any(User.class));
    }
  }
}