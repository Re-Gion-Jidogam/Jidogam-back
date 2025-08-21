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
import region.jidogam.domain.user.exception.UserNicknameLengthException;
import region.jidogam.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 서비스 단위테스트")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

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

  @Nested
  @DisplayName("닉네임 중복 체크")
  class CheckNicknameTest {
    @Test
    @DisplayName("중복되는 닉네임이 없음")
    void success() {
      //given
      String nickname = "중복아닌닉네임";
      when(userRepository.existsByNickname(nickname)).thenReturn(false);

      //when
      userService.validateNickname(nickname);

      //then
      verify(userRepository, times(1)).existsByNickname(nickname);
    }

    @Test
    @DisplayName("중복되는 닉네임이 있음")
    void failsWhenNicknameConflicted() {
      // given
      String nickname = "중복된닉네임";
      when(userRepository.existsByNickname(nickname)).thenReturn(true);

      // when & then
      assertThrows(UserNicknameConflictException.class, () -> userService.validateNickname(nickname));
      verify(userRepository, times(1)).existsByNickname(nickname);
    }

    @Test
    @DisplayName("닉네임이 Null임")
    void failsWhenNicknameIsNull() {
      // given
      String nickname = null;

      // when & then
      assertThrows(UserNicknameLengthException.class, () -> userService.validateNickname(nickname));
      verify(userRepository, never()).existsByNickname(nickname);
    }

    @Test
    @DisplayName("닉네임이 비어있음")
    void failsWhenEmptyNickname() {
      // given
      String nickname = "";

      // when & then
      assertThrows(UserNicknameLengthException.class, () -> userService.validateNickname(nickname));
      verify(userRepository, never()).existsByNickname(nickname);
    }

    @Test
    @DisplayName("닉네임이 공백으로만 되어있음")
    void failsWhenBlankNickname() {
      // given
      String nickname = "     ";

      // when & then
      assertThrows(UserNicknameLengthException.class, () -> userService.validateNickname(nickname));
      verify(userRepository, never()).existsByNickname(nickname);
    }

    @Test
    @DisplayName("길이가 2미만인 닉네임")
    void failsWhenNicknameLengthTooShort() {
      // given
      String nickname = "0";

      // when & then
      assertThrows(UserNicknameLengthException.class, () -> userService.validateNickname(nickname));
      verify(userRepository, never()).existsByNickname(nickname);
    }

    @Test
    @DisplayName("길이가 20초과인 닉네임")
    void failsWhenNicknameLengthTooLong() {
      // given
      String nickname = "012345678901234567890";

      // when & then
      assertThrows(UserNicknameLengthException.class, () -> userService.validateNickname(nickname));
      verify(userRepository, never()).existsByNickname(nickname);
    }
  }
}