package region.jidogam.domain.user.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserEmailConflictException;
import region.jidogam.domain.user.exception.UserNicknameConflictException;
import region.jidogam.domain.user.repository.UserRepository;
import region.jidogam.domain.user.service.UserService;

@SpringBootTest
@Transactional
@DisplayName("사용자 회원가입 서비스-레포지토리 통합 테스트")
public class UserCreateIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  void setup() {
    User user = User.builder()
        .nickname("지도감")
        .email("jidogam@email.com")
        .password("password1234")
        .build();

    userRepository.save(user);
  }

  @Test
  @DisplayName("성공")
  void success() {
    //given
    String nickname = "테스트유저";
    String email = "test@email.com";
    String password = "password1234";

    UserCreateRequest userCreateRequest = new UserCreateRequest(nickname, email, password);

    //when
    userService.create(userCreateRequest);

    // then
    Optional<User> savedUser = userRepository.findByEmail(email);
    assertThat(savedUser).isPresent();
    assertThat(savedUser.get().getNickname()).isEqualTo(nickname);
  }

  @Test
  @DisplayName("이메일이 중복이면 회원가입 실패")
  void failsWhenEmailConflicted() {
    //given
    String nickname = "테스트유저";
    String email = "jidogam@email.com";
    String password = "password1234";

    UserCreateRequest userCreateRequest = new UserCreateRequest(nickname, email, password);

    //when & when
    assertThrows(UserEmailConflictException.class, ()-> userService.create(userCreateRequest));

    Optional<User> savedUser = userRepository.findByNickname(nickname);
    assertThat(savedUser).isNotPresent();
  }

  @Test
  @DisplayName("닉네임이 중복이면 회원가입 실패")
  void failsWhenNicknameConflicted() {
    //given
    String nickname = "지도감";
    String email = "test@email.com";
    String password = "password1234";

    UserCreateRequest userCreateRequest = new UserCreateRequest(nickname, email, password);

    //when & then
    assertThrows(UserNicknameConflictException.class, ()-> userService.create(userCreateRequest));

    // then
    Optional<User> savedUser = userRepository.findByEmail(email);
    assertThat(savedUser).isNotPresent();
  }
}
