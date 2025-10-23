package region.jidogam.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.h2.command.dml.MergeUsing.When;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import region.jidogam.domain.auth.entity.EmailAuthCode;
import region.jidogam.domain.auth.exception.EmailAuthNotFoundException;
import region.jidogam.domain.auth.repository.EmailAuthCodeRepository;
import region.jidogam.domain.guidebook.service.GuidebookService;
import region.jidogam.domain.stamp.entity.Stamp;
import region.jidogam.domain.stamp.repository.StampRepository;
import region.jidogam.domain.stamp.service.StampService;
import region.jidogam.domain.user.UserMapper;
import region.jidogam.domain.user.dto.UserDto;
import region.jidogam.domain.user.exception.UnverifiedEmailException;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.infrastructure.jwt.JwtProvider;
import region.jidogam.infrastructure.jwt.RefreshToken;
import region.jidogam.infrastructure.jwt.RefreshTokenService;
import region.jidogam.infrastructure.jwt.dto.TokenPair;
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.InvalidEmailFormatException;
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

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private RefreshTokenService refreshTokenService;

  @Mock
  private StampRepository stampRepository;

  @Mock
  private EmailAuthCodeRepository emailAuthCodeRepository;

  @Spy
  private UserMapper userMapper;

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
      EmailAuthCode emailAuthCode = mock(EmailAuthCode.class);

      UserCreateRequest userCreateRequest = new UserCreateRequest(nickname, email, password);

      when(userRepository.existsByNickname(nickname)).thenReturn(false);
      when(userRepository.existsByEmail(email)).thenReturn(false);

      when(emailAuthCodeRepository.findByEmail(email)).thenReturn(Optional.of(emailAuthCode));
      when(emailAuthCode.getUsed()).thenReturn(true);

      when(passwordEncoder.encode("password1234")).thenReturn("encordedPassword1234");

      User user = User.builder()
          .nickname(userCreateRequest.nickname())
          .email(userCreateRequest.email())
          .password(passwordEncoder.encode(userCreateRequest.password()))
          .build();

      when(userRepository.save(any(User.class))).thenReturn(user);
      when(jwtProvider.generateAccessToken(any(User.class))).thenReturn("accessToken");

      RefreshToken refreshToken = RefreshToken.builder()
          .refreshToken("refreshToken")
          .build();
      when(refreshTokenService.create(any(User.class))).thenReturn(refreshToken);

      //when
      TokenPair tokenPair = userService.create(userCreateRequest);

      //then
      verify(userRepository, times(1)).save(any(User.class));
      assertNotNull(tokenPair);
      assertEquals("accessToken", tokenPair.accessToken());
      assertEquals("refreshToken", tokenPair.refreshToken());
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

    @Test
    @DisplayName("인증 코드 발송 이력이 없는 이메일")
    void failsWhenEmailAuthCodeNotFound() {
      //given
      String nickname = "테스트유저";
      String email = "test@email.com";
      String password = "password1234";

      UserCreateRequest userCreateRequest = new UserCreateRequest(nickname, email, password);

      when(userRepository.existsByNickname(nickname)).thenReturn(false);
      when(userRepository.existsByEmail(email)).thenReturn(false);

      when(emailAuthCodeRepository.findByEmail(email)).thenReturn(Optional.empty());

      //when & then
      assertThrows(EmailAuthNotFoundException.class, () -> userService.create(userCreateRequest));

      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("인증 인증 이력이 없는 이메일")
    void failsWhenEmailAuthCodeNotVarified() {
      //given
      String nickname = "테스트유저";
      String email = "test@email.com";
      String password = "password1234";
      EmailAuthCode emailAuthCode = mock(EmailAuthCode.class);

      UserCreateRequest userCreateRequest = new UserCreateRequest(nickname, email, password);

      when(userRepository.existsByNickname(nickname)).thenReturn(false);
      when(userRepository.existsByEmail(email)).thenReturn(false);

      when(emailAuthCodeRepository.findByEmail(email)).thenReturn(Optional.of(emailAuthCode));
      when(emailAuthCode.getUsed()).thenReturn(false);

      //when & then
      assertThrows(UnverifiedEmailException.class, () -> userService.create(userCreateRequest));

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
      assertThrows(UserNicknameConflictException.class,
          () -> userService.validateNickname(nickname));
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

  @Nested
  @DisplayName("이메일 중복 체크")
  class ValidateEmailTest {

    @Test
    @DisplayName("사용 가능한 이메일")
    void success() {
      //given
      String email = "test@test.com";
      when(userRepository.existsByEmail(email)).thenReturn(false);

      //when
      userService.validateEmail(email);

      //then
      verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("중복되는 이메일이 있음")
    void failsWhenEmailConflicted() {
      //given
      String email = "test@test.com";
      when(userRepository.existsByEmail(email)).thenReturn(true);

      //when & then
      assertThrows(UserEmailConflictException.class, () -> userService.validateEmail(email));
      verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("이메일 형식에 맞지 않음")
    void failsWhenEmailFormatInvalid() {
      //given
      String email = "test.com";

      //when & then
      assertThrows(InvalidEmailFormatException.class, () -> userService.validateEmail(email));
      verify(userRepository, never()).existsByEmail(null);
    }

    @Test
    @DisplayName("이메일이 비어있음")
    void failsWhenEmailIsEmpty() {
      //given
      String email = " ";

      //when & then
      assertThrows(InvalidEmailFormatException.class, () -> userService.validateEmail(email));
      verify(userRepository, never()).existsByEmail(null);
    }

    @Test
    @DisplayName("이메일이 공백으로만 되어있음")
    void failsWhenEmailIsBlank() {
      //given
      String email = "    ";

      //when & then
      assertThrows(InvalidEmailFormatException.class, () -> userService.validateEmail(email));
      verify(userRepository, never()).existsByEmail(null);
    }

    @Test
    @DisplayName("이메일이 null임")
    void failsWhenEmailIsNull() {
      //given
      String email = null;

      //when & then
      assertThrows(InvalidEmailFormatException.class, () -> userService.validateEmail(email));
      verify(userRepository, never()).existsByEmail(null);
    }

  }

  @Nested
  @DisplayName("사용자 정보 조회")
  class GetUserInfoTest {

    @Test
    @DisplayName("성공, 도장 찍은 내역 없음")
    void success() {
      //given
      UUID userId = UUID.randomUUID();
      User user = User.builder()
          .nickname("테스트유저")
          .profileImageUrl("https://test.com/url")
          .exp(1L)
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId)).thenReturn(
          Optional.empty());

      //when
      UserDto userInfo = userService.getUserInfo(userId);

      //then
      assertNotNull(userInfo);
      assertEquals(user.getNickname(), userInfo.nickname());
      assertEquals(user.getProfileImageUrl(), userInfo.profileUrl());
      assertNull(userInfo.lastStampedDate());
    }

    @Test
    @DisplayName("성공, 최근 도장 찍은 내역 있음")
    void successWhenLastStampExist() {
      //given
      UUID userId = UUID.randomUUID();
      User user = User.builder()
          .nickname("테스트유저")
          .profileImageUrl("https://test.com/url")
          .exp(1L)
          .build();

      Stamp mockStamp = mock(Stamp.class);
      LocalDateTime stampCreatedAt = LocalDateTime.now();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId)).thenReturn(
          Optional.of(mockStamp));
      when(mockStamp.getCreatedAt()).thenReturn(stampCreatedAt);

      //when
      UserDto userInfo = userService.getUserInfo(userId);

      //then
      assertNotNull(userInfo);
      assertEquals(user.getNickname(), userInfo.nickname());
      assertEquals(user.getProfileImageUrl(), userInfo.profileUrl());
      assertEquals(stampCreatedAt, userInfo.lastStampedDate());
    }

    @Test
    @DisplayName("존재하지 않는 사용자")
    void failsWhenUserNotFound() {
      //given
      UUID userId = UUID.randomUUID();

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class, () -> userService.getUserInfo(userId));
    }
  }
  @Nested
  @DisplayName("사용자")
  class GetUsersGuideBookTest {

    @Test
    @DisplayName("파라미터가 없고 검색한 유저와 가이드북 소유자가 같은 경우")
    void success() {
      //given
      UUID userId = UUID.randomUUID();



      //when

      //then

    }
  }
}
