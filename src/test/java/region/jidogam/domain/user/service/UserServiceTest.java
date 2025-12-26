package region.jidogam.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import region.jidogam.common.dto.SortDirection;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.common.util.CursorCodecUtil;
import region.jidogam.domain.area.entity.Area;
import region.jidogam.domain.area.entity.Area.AreaType;
import region.jidogam.domain.auth.entity.EmailAuthCode;
import region.jidogam.domain.auth.exception.EmailAuthNotFoundException;
import region.jidogam.domain.auth.repository.EmailAuthCodeRepository;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.dto.ParticipationFilter;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookParticipation;
import region.jidogam.domain.guidebook.mapper.GuidebookMapper;
import region.jidogam.domain.guidebook.repository.GuidebookParticipationRepository;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.mapper.PlaceMapper;
import region.jidogam.domain.stamp.dto.StampSearchRequest;
import region.jidogam.domain.stamp.dto.StampSortBy;
import region.jidogam.domain.stamp.entity.Stamp;
import region.jidogam.domain.stamp.repository.StampRepository;
import region.jidogam.domain.user.dto.GuidebookParticipationCursor;
import region.jidogam.domain.user.dto.GuidebookParticipationResponse;
import region.jidogam.domain.user.dto.GuidebookParticipationSearchRequest;
import region.jidogam.domain.user.dto.GuidebookParticipationSortBy;
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.dto.UserDto;
import region.jidogam.domain.user.dto.UserGuideBookSortBy;
import region.jidogam.domain.user.dto.UserGuidebookSearchRequest;
import region.jidogam.domain.user.dto.UserUpdateRequest;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.InvalidEmailFormatException;
import region.jidogam.domain.user.exception.UnverifiedEmailException;
import region.jidogam.domain.user.exception.UserAlreadyDeletedException;
import region.jidogam.domain.user.exception.UserEmailConflictException;
import region.jidogam.domain.user.exception.UserExpException;
import region.jidogam.domain.user.exception.UserNicknameConflictException;
import region.jidogam.domain.user.exception.UserNicknameLengthException;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.exception.UserRestorePeriodExpiredException;
import region.jidogam.domain.user.mapper.UserMapper;
import region.jidogam.domain.user.repository.UserRepository;
import region.jidogam.domain.user.util.LevelCalculator;
import region.jidogam.infrastructure.jwt.JwtProvider;
import region.jidogam.infrastructure.jwt.RefreshToken;
import region.jidogam.infrastructure.jwt.RefreshTokenService;
import region.jidogam.infrastructure.jwt.dto.TokenPair;

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

  @Mock
  private GuidebookRepository guidebookRepository;

  @Mock
  private GuidebookParticipationRepository guidebookParticipantRepository;

  @Mock
  private CursorCodecUtil cursorCodecUtil;

  @Spy
  private UserMapper userMapper;

  @Mock
  private GuidebookMapper guidebookMapper;

  @Spy
  private PlaceMapper placeMapper;

  @Spy
  private LevelCalculator levelCalculator;

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
  @DisplayName("사용자 가이드북 목록 조회")
  class GetUsersGuideBookTest {

    @Test
    @DisplayName("파라미터가 없고 검색한 유저와 가이드북 소유자가 같은 경우")
    void success() {
      //given
      UUID userId = UUID.randomUUID();

      //when

      //then

    }

    @Test
    @DisplayName("실패 - 탈퇴한 사용자의 가이드북 조회")
    void failsWhenAuthorIsDeleted() {
      //given
      UUID userId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      User deletedAuthor = User.builder()
          .nickname("탈퇴한유저")
          .email("deleted@test.com")
          .password("password")
          .build();
      deletedAuthor.softDelete();

      UserGuidebookSearchRequest request = new UserGuidebookSearchRequest(
          null,
          UserGuideBookSortBy.CREATED_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      when(userRepository.findById(authorId)).thenReturn(Optional.of(deletedAuthor));

      //when & then
      assertThrows(UserNotFoundException.class,
          () -> userService.getUserGuidebookList(userId, authorId, request));
      verify(userRepository, times(1)).findById(authorId);
      verify(guidebookRepository, never()).searchGuidebookByAuthorId(
          any(UUID.class), any(), any(), any(), any(), any(Integer.class), any(Boolean.class)
      );
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자의 가이드북 조회")
    void failsWhenAuthorNotFound() {
      //given
      UUID userId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();

      UserGuidebookSearchRequest request = new UserGuidebookSearchRequest(
          null,
          UserGuideBookSortBy.CREATED_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      when(userRepository.findById(authorId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class,
          () -> userService.getUserGuidebookList(userId, authorId, request));
      verify(userRepository, times(1)).findById(authorId);
      verify(guidebookRepository, never()).searchGuidebookByAuthorId(
          any(UUID.class), any(), any(), any(), any(), any(Integer.class), any(Boolean.class)
      );
    }
  }

  @Nested
  @DisplayName("사용자 정보 수정")
  class UpdateUserTest {

    @Test
    @DisplayName("성공 - 모든 필드 업데이트")
    void updateAllFields() {
      //given
      UUID userId = UUID.randomUUID();
      String newNickname = "새닉네임";
      String newPassword = "newPassword1234";
      String newProfileImageUrl = "https://new-image.com/profile.jpg";

      UserUpdateRequest request = new UserUpdateRequest(
          newNickname,
          newPassword,
          newProfileImageUrl
      );

      User user = User.builder()
          .nickname("기존닉네임")
          .email("test@email.com")
          .password("oldPassword")
          .profileImageUrl("https://old-image.com/profile.jpg")
          .exp(10L)
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userRepository.existsByNickname(newNickname)).thenReturn(false);
      when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword1234");
      when(userRepository.save(any(User.class))).thenReturn(user);
      when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId)).thenReturn(
          Optional.empty());

      //when
      UserDto result = userService.update(userId, request);

      //then
      assertNotNull(result);
      assertEquals(newNickname, result.nickname());
      assertNull(result.lastStampedDate());
      verify(userRepository, times(1)).findById(userId);
      verify(userRepository, times(1)).existsByNickname(newNickname);
      verify(passwordEncoder, times(1)).encode(newPassword);
      verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("성공 - 닉네임만 업데이트")
    void updateNicknameOnly() {
      //given
      UUID userId = UUID.randomUUID();
      String newNickname = "새닉네임";

      UserUpdateRequest request = new UserUpdateRequest(
          newNickname,
          null,
          null
      );

      User user = User.builder()
          .nickname("기존닉네임")
          .email("test@email.com")
          .password("oldPassword")
          .profileImageUrl("https://old-image.com/profile.jpg")
          .exp(10L)
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userRepository.existsByNickname(newNickname)).thenReturn(false);
      when(userRepository.save(any(User.class))).thenReturn(user);
      when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId)).thenReturn(
          Optional.empty());

      //when
      UserDto result = userService.update(userId, request);

      //then
      assertNotNull(result);
      assertEquals(newNickname, result.nickname());
      verify(userRepository, times(1)).findById(userId);
      verify(userRepository, times(1)).existsByNickname(newNickname);
      verify(passwordEncoder, never()).encode(any());
      verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("성공 - 비밀번호만 업데이트")
    void updatePasswordOnly() {
      //given
      UUID userId = UUID.randomUUID();
      String newPassword = "newPassword1234";

      UserUpdateRequest request = new UserUpdateRequest(
          null,
          newPassword,
          null
      );

      User user = User.builder()
          .nickname("기존닉네임")
          .email("test@email.com")
          .password("oldPassword")
          .profileImageUrl("https://old-image.com/profile.jpg")
          .exp(10L)
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword1234");
      when(userRepository.save(any(User.class))).thenReturn(user);
      when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId)).thenReturn(
          Optional.empty());

      //when
      UserDto result = userService.update(userId, request);

      //then
      assertNotNull(result);
      assertEquals("기존닉네임", result.nickname());
      verify(userRepository, times(1)).findById(userId);
      verify(userRepository, never()).existsByNickname(any());
      verify(passwordEncoder, times(1)).encode(newPassword);
      verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("성공 - 프로필 이미지만 업데이트")
    void updateProfileImageOnly() {
      //given
      UUID userId = UUID.randomUUID();
      String newProfileImageUrl = "https://new-image.com/profile.jpg";

      UserUpdateRequest request = new UserUpdateRequest(
          null,
          null,
          newProfileImageUrl
      );

      User user = User.builder()
          .nickname("기존닉네임")
          .email("test@email.com")
          .password("oldPassword")
          .profileImageUrl("https://old-image.com/profile.jpg")
          .exp(10L)
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userRepository.save(any(User.class))).thenReturn(user);
      when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId)).thenReturn(
          Optional.empty());

      //when
      UserDto result = userService.update(userId, request);

      //then
      assertNotNull(result);
      assertEquals("기존닉네임", result.nickname());
      verify(userRepository, times(1)).findById(userId);
      verify(userRepository, never()).existsByNickname(any());
      verify(passwordEncoder, never()).encode(any());
      verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("성공 - 최근 도장 찍은 내역 있음")
    void successWhenLastStampExist() {
      //given
      UUID userId = UUID.randomUUID();
      String newNickname = "새닉네임";
      String newPassword = "newPassword1234";
      String newProfileImageUrl = "https://new-image.com/profile.jpg";

      UserUpdateRequest request = new UserUpdateRequest(
          newNickname,
          newPassword,
          newProfileImageUrl
      );

      User user = User.builder()
          .nickname("기존닉네임")
          .email("test@email.com")
          .password("oldPassword")
          .profileImageUrl("https://old-image.com/profile.jpg")
          .exp(10L)
          .build();

      Stamp mockStamp = mock(Stamp.class);
      LocalDateTime stampCreatedAt = LocalDateTime.now();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userRepository.existsByNickname(newNickname)).thenReturn(false);
      when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword1234");
      when(userRepository.save(any(User.class))).thenReturn(user);
      when(stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId)).thenReturn(
          Optional.of(mockStamp));
      when(mockStamp.getCreatedAt()).thenReturn(stampCreatedAt);

      //when
      UserDto result = userService.update(userId, request);

      //then
      assertNotNull(result);
      assertEquals(newNickname, result.nickname());
      assertEquals(stampCreatedAt, result.lastStampedDate());
      verify(userRepository, times(1)).findById(userId);
      verify(userRepository, times(1)).existsByNickname(newNickname);
      verify(passwordEncoder, times(1)).encode(newPassword);
      verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("실패 - 비밀번호가 빈 문자열")
    void failsWhenPasswordIsBlank() {
      //given
      UUID userId = UUID.randomUUID();
      String blankPassword = "   ";

      UserUpdateRequest request = new UserUpdateRequest(
          null,
          blankPassword,
          null
      );

      User user = User.builder()
          .nickname("기존닉네임")
          .email("test@email.com")
          .password("oldPassword")
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      //when & then
      assertThrows(region.jidogam.domain.user.exception.UserPasswordLengthException.class,
          () -> userService.update(userId, request));
      verify(userRepository, times(1)).findById(userId);
      verify(passwordEncoder, never()).encode(any());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("실패 - 비밀번호 길이가 8 미만")
    void failsWhenPasswordTooShort() {
      //given
      UUID userId = UUID.randomUUID();
      String shortPassword = "short";

      UserUpdateRequest request = new UserUpdateRequest(
          null,
          shortPassword,
          null
      );

      User user = User.builder()
          .nickname("기존닉네임")
          .email("test@email.com")
          .password("oldPassword")
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      //when & then
      assertThrows(region.jidogam.domain.user.exception.UserPasswordLengthException.class,
          () -> userService.update(userId, request));
      verify(userRepository, times(1)).findById(userId);
      verify(passwordEncoder, never()).encode(any());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("실패 - 닉네임 중복")
    void failsWhenNicknameDuplicated() {
      //given
      UUID userId = UUID.randomUUID();
      String duplicatedNickname = "중복닉네임";

      UserUpdateRequest request = new UserUpdateRequest(
          duplicatedNickname,
          null,
          null
      );

      User user = User.builder()
          .nickname("기존닉네임")
          .email("test@email.com")
          .password("oldPassword")
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userRepository.existsByNickname(duplicatedNickname)).thenReturn(true);

      //when & then
      assertThrows(UserNicknameConflictException.class,
          () -> userService.update(userId, request));
      verify(userRepository, times(1)).findById(userId);
      verify(userRepository, times(1)).existsByNickname(duplicatedNickname);
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("실패 - 닉네임 길이 초과")
    void failsWhenNicknameTooLong() {
      //given
      UUID userId = UUID.randomUUID();
      String tooLongNickname = "012345678901234567890"; // 21자

      UserUpdateRequest request = new UserUpdateRequest(
          tooLongNickname,
          null,
          null
      );

      User user = User.builder()
          .nickname("기존닉네임")
          .email("test@email.com")
          .password("oldPassword")
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      //when & then
      assertThrows(UserNicknameLengthException.class,
          () -> userService.update(userId, request));
      verify(userRepository, times(1)).findById(userId);
      verify(userRepository, never()).existsByNickname(any());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("실패 - 닉네임 길이 미만")
    void failsWhenNicknameTooShort() {
      //given
      UUID userId = UUID.randomUUID();
      String tooShortNickname = "a"; // 1자

      UserUpdateRequest request = new UserUpdateRequest(
          tooShortNickname,
          null,
          null
      );

      User user = User.builder()
          .nickname("기존닉네임")
          .email("test@email.com")
          .password("oldPassword")
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      //when & then
      assertThrows(UserNicknameLengthException.class,
          () -> userService.update(userId, request));
      verify(userRepository, times(1)).findById(userId);
      verify(userRepository, never()).existsByNickname(any());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자")
    void failsWhenUserNotFound() {
      //given
      UUID userId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest(
          "새닉네임",
          "newPassword1234",
          "https://new-image.com/profile.jpg"
      );

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class, () -> userService.update(userId, request));
      verify(userRepository, times(1)).findById(userId);
      verify(userRepository, never()).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("사용자 경험치 증가")
  class IncreaseUserExpTest {

    @Test
    @DisplayName("성공 - 정상적으로 경험치 증가")
    void success() {
      //given
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .exp(100L)
          .build();

      int expToAdd = 50;

      //when
      userService.increaseUserExp(user, expToAdd);

      //then
      assertEquals(150L, user.getExp());
    }

    @Test
    @DisplayName("성공 - 경험치가 0일 때 증가")
    void successWhenExpIsZero() {
      //given
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .exp(0L)
          .build();

      int expToAdd = 100;

      //when
      userService.increaseUserExp(user, expToAdd);

      //then
      assertEquals(100L, user.getExp());
    }

    @Test
    @DisplayName("성공 - 큰 경험치 값 증가")
    void successWithLargeExp() {
      //given
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .exp(1000000L)
          .build();

      int expToAdd = 1000000;

      //when
      userService.increaseUserExp(user, expToAdd);

      //then
      assertEquals(2000000L, user.getExp());
    }

    @Test
    @DisplayName("실패 - 음수 경험치를 추가하려고 할 때")
    void failsWhenExpIsNegative() {
      //given
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .exp(100L)
          .build();

      int negativeExp = -50;

      //when & then
      assertThrows(UserExpException.class, () -> userService.increaseUserExp(user, negativeExp));
      assertEquals(100L, user.getExp()); // 경험치는 변경되지 않아야 함
    }
  }

  @Nested
  @DisplayName("사용자 경험치 감소")
  class DecreaseUserExpTest {

    @Test
    @DisplayName("성공 - 정상적으로 경험치 감소")
    void success() {
      //given
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .exp(100L)
          .build();

      int expToDecrease = 50;

      //when
      userService.decreaseUserExp(user, expToDecrease);

      //then
      assertEquals(50L, user.getExp());
    }

    @Test
    @DisplayName("성공 - 경험치가 0이 되는 경우")
    void successWhenExpBecomesZero() {
      //given
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .exp(100L)
          .build();

      int expToDecrease = 100;

      //when
      userService.decreaseUserExp(user, expToDecrease);

      //then
      assertEquals(0L, user.getExp());
    }

    @Test
    @DisplayName("성공 - 감소량이 현재 경험치보다 많을 때 0으로 설정")
    void successWhenExpGoesNegative() {
      //given
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .exp(50L)
          .build();

      int expToDecrease = 100;

      //when
      userService.decreaseUserExp(user, expToDecrease);

      //then
      assertEquals(0L, user.getExp()); // 음수가 되지 않고 0으로 설정되어야 함
    }

    @Test
    @DisplayName("성공 - 경험치가 0일 때 감소")
    void successWhenCurrentExpIsZero() {
      //given
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .exp(0L)
          .build();

      int expToDecrease = 50;

      //when
      userService.decreaseUserExp(user, expToDecrease);

      //then
      assertEquals(0L, user.getExp());
    }

    @Test
    @DisplayName("실패 - 음수 경험치를 감소하려고 할 때")
    void failsWhenExpIsNegative() {
      //given
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .exp(100L)
          .build();

      int negativeExp = -50;

      //when & then
      assertThrows(UserExpException.class, () -> userService.decreaseUserExp(user, negativeExp));
      assertEquals(100L, user.getExp()); // 경험치는 변경되지 않아야 함
    }
  }

  @Nested
  @DisplayName("사용자 도장 목록 조회")
  class GetUserStampsTest {

    private UUID testUserId;
    private User testUser;
    private Area testArea;
    private Place testPlace1;
    private Place testPlace2;
    private Place testPlace3;
    private Stamp testStamp1;
    private Stamp testStamp2;
    private Stamp testStamp3;

    @BeforeEach
    void setUp() {
      testUserId = UUID.randomUUID();

      testUser = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .build();

      testArea = Area.builder()
          .sido("서울특별시")
          .sigungu("강남구")
          .sigunguCode("1168000000")
          .weight(1.0)
          .type(AreaType.NORMAL)
          .build();

      testPlace1 = Place.builder()
          .area(testArea)
          .name("장소1")
          .x(new BigDecimal("127.0"))
          .y(new BigDecimal("37.0"))
          .address("서울시 강남구")
          .category("카페")
          .build();

      testPlace2 = Place.builder()
          .area(testArea)
          .name("장소2")
          .x(new BigDecimal("127.1"))
          .y(new BigDecimal("37.1"))
          .address("서울시 서초구")
          .category("식당")
          .build();

      testPlace3 = Place.builder()
          .area(testArea)
          .name("장소3")
          .x(new BigDecimal("127.2"))
          .y(new BigDecimal("37.2"))
          .address("서울시 송파구")
          .category("공원")
          .build();

      testStamp1 = Stamp.builder()
          .user(testUser)
          .place(testPlace1)
          .build();

      testStamp2 = Stamp.builder()
          .user(testUser)
          .place(testPlace2)
          .build();

      testStamp3 = Stamp.builder()
          .user(testUser)
          .place(testPlace3)
          .build();
    }

    @Test
    @DisplayName("성공 - 기본 조회 (파라미터 없음)")
    void success() {
      //given
      StampSearchRequest request = new StampSearchRequest(
          StampSortBy.CREATED_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      List<Stamp> stamps = Arrays.asList(testStamp1, testStamp2);

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeStampCursor(null)).thenReturn(null);
      when(stampRepository.searchStampsByUserId(
          testUserId,
          null,
          null,
          StampSortBy.CREATED_AT,
          SortDirection.DESC,
          21
      )).thenReturn(stamps);
      when(stampRepository.countStampsByUserId(testUserId, null)).thenReturn(2L);

      //when
      CursorPageResponseDto<PlaceResponse> result = userService.getUserStamps(testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(2L, result.totalCount());
      assertFalse(result.hasNext());
      assertNull(result.nextCursor());
      assertEquals("createdAt", result.sortBy());
      assertEquals(SortDirection.DESC, result.sortDirection());
      verify(userRepository, times(1)).findById(testUserId);
      verify(stampRepository, times(1)).searchStampsByUserId(
          testUserId, null, null, StampSortBy.CREATED_AT, SortDirection.DESC, 21
      );
      verify(stampRepository, times(1)).countStampsByUserId(testUserId, null);
    }

    @Test
    @DisplayName("성공 - 키워드 검색")
    void successWithKeyword() {
      //given
      StampSearchRequest request = new StampSearchRequest(
          StampSortBy.CREATED_AT,
          SortDirection.DESC,
          null,
          20,
          "카페"
      );

      List<Stamp> stamps = Arrays.asList(testStamp1);

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeStampCursor(null)).thenReturn(null);
      when(stampRepository.searchStampsByUserId(
          testUserId,
          null,
          "카페",
          StampSortBy.CREATED_AT,
          SortDirection.DESC,
          21
      )).thenReturn(stamps);
      when(stampRepository.countStampsByUserId(testUserId, "카페")).thenReturn(1L);

      //when
      CursorPageResponseDto<PlaceResponse> result = userService.getUserStamps(testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(1L, result.totalCount());
      assertFalse(result.hasNext());
      verify(stampRepository, times(1)).searchStampsByUserId(
          testUserId, null, "카페", StampSortBy.CREATED_AT, SortDirection.DESC, 21
      );
      verify(stampRepository, times(1)).countStampsByUserId(testUserId, "카페");
    }

    @Test
    @DisplayName("성공 - hasNext가 true인 경우")
    void successWithHasNext() {
      //given
      StampSearchRequest request = new StampSearchRequest(
          StampSortBy.CREATED_AT,
          SortDirection.DESC,
          null,
          2,
          null
      );

      // Arrays.asList는 불변 리스트이므로 ArrayList로 변경
      List<Stamp> stamps = new ArrayList<>(Arrays.asList(testStamp1, testStamp2, testStamp3));

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeStampCursor(null)).thenReturn(null);
      when(cursorCodecUtil.encodeNextCursor(any(PlaceResponse.class), any(StampSortBy.class)))
          .thenReturn("encodedCursor");
      when(stampRepository.searchStampsByUserId(
          testUserId,
          null,
          null,
          StampSortBy.CREATED_AT,
          SortDirection.DESC,
          3
      )).thenReturn(stamps);
      when(stampRepository.countStampsByUserId(testUserId, null)).thenReturn(10L);

      //when
      CursorPageResponseDto<PlaceResponse> result = userService.getUserStamps(testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(10L, result.totalCount());
      assertTrue(result.hasNext());
      assertNotNull(result.nextCursor());
    }

    @Test
    @DisplayName("성공 - 빈 결과")
    void successWithEmptyResult() {
      //given
      StampSearchRequest request = new StampSearchRequest(
          StampSortBy.CREATED_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeStampCursor(null)).thenReturn(null);
      when(stampRepository.searchStampsByUserId(
          testUserId,
          null,
          null,
          StampSortBy.CREATED_AT,
          SortDirection.DESC,
          21
      )).thenReturn(Arrays.asList());
      when(stampRepository.countStampsByUserId(testUserId, null)).thenReturn(0L);

      //when
      CursorPageResponseDto<PlaceResponse> result = userService.getUserStamps(testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(0, result.size());
      assertEquals(0L, result.totalCount());
      assertFalse(result.hasNext());
      assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("성공 - 오름차순 정렬")
    void successWithAscendingOrder() {
      //given
      StampSearchRequest request = new StampSearchRequest(
          StampSortBy.CREATED_AT,
          SortDirection.ASC,
          null,
          20,
          null
      );

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeStampCursor(null)).thenReturn(null);
      when(stampRepository.searchStampsByUserId(
          testUserId,
          null,
          null,
          StampSortBy.CREATED_AT,
          SortDirection.ASC,
          21
      )).thenReturn(Arrays.asList());
      when(stampRepository.countStampsByUserId(testUserId, null)).thenReturn(0L);

      //when
      CursorPageResponseDto<PlaceResponse> result = userService.getUserStamps(testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(SortDirection.ASC, result.sortDirection());
      verify(stampRepository, times(1)).searchStampsByUserId(
          testUserId, null, null, StampSortBy.CREATED_AT, SortDirection.ASC, 21
      );
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자")
    void failsWhenUserNotFound() {
      //given
      UUID currentUserId = UUID.randomUUID();
      UUID differentUserId = UUID.randomUUID();

      StampSearchRequest request = new StampSearchRequest(
          StampSortBy.CREATED_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class,
          () -> userService.getUserStamps(testUserId, request));
      verify(userRepository, times(1)).findById(testUserId);
      verify(stampRepository, never()).searchStampsByUserId(
          any(UUID.class), any(), any(), any(StampSortBy.class), any(SortDirection.class),
          any(Integer.class)
      );
    }
  }

  @Nested
  @DisplayName("사용자 참여 가이드북 목록 조회")
  class GetUserParticipationTest {

    private UUID testUserId;
    private UUID currentUserId;
    private User testUser;
    private User author;
    private Guidebook testGuidebook1;
    private Guidebook testGuidebook2;
    private Guidebook testGuidebook3;
    private GuidebookParticipation testParticipation1;
    private GuidebookParticipation testParticipation2;
    private GuidebookParticipation testParticipation3;

    @BeforeEach
    void setUp() {
      testUserId = UUID.randomUUID();
      currentUserId = UUID.randomUUID();

      testUser = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .build();

      author = User.builder()
          .nickname("작성자")
          .email("author@test.com")
          .password("password")
          .build();

      testGuidebook1 = Guidebook.builder()
          .author(author)
          .title("가이드북1")
          .description("설명1")
          .emoji("🎉")
          .color("#FF0000")
          .build();

      testGuidebook2 = Guidebook.builder()
          .author(author)
          .title("가이드북2")
          .description("설명2")
          .emoji("🌟")
          .color("#00FF00")
          .build();

      testGuidebook3 = Guidebook.builder()
          .author(author)
          .title("가이드북3")
          .description("설명3")
          .emoji("🎈")
          .color("#0000FF")
          .build();

      testParticipation1 = GuidebookParticipation.builder()
          .user(testUser)
          .guidebook(testGuidebook1)
          .lastActivityAt(LocalDateTime.now().minusDays(1))
          .isCompleted(false)
          .build();

      testParticipation2 = GuidebookParticipation.builder()
          .user(testUser)
          .guidebook(testGuidebook2)
          .lastActivityAt(LocalDateTime.now().minusDays(2))
          .isCompleted(true)
          .build();

      testParticipation3 = GuidebookParticipation.builder()
          .user(testUser)
          .guidebook(testGuidebook3)
          .lastActivityAt(LocalDateTime.now().minusDays(3))
          .isCompleted(false)
          .build();
    }

    @Test
    @DisplayName("성공 - 기본 조회 (파라미터 없음)")
    void success() {
      //given
      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          null,
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      List<GuidebookParticipation> participations = Arrays.asList(
          testParticipation1, testParticipation2
      );

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeParticipantGuidebookCursor(null)).thenReturn(null);
      when(guidebookParticipantRepository.searchParticipatingGuidebooks(
          testUserId,
          null,
          null,
          SortDirection.DESC,
          null,
          21
      )).thenReturn(participations);
      when(guidebookParticipantRepository.countParticipatingGuidebooks(
          testUserId,
          null,
          null
      )).thenReturn(2L);

      //when
      CursorPageResponseDto<GuidebookParticipationResponse> result =
          userService.getUserParticipation(currentUserId, testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(2L, result.totalCount());
      assertFalse(result.hasNext());
      assertNull(result.nextCursor());
      assertEquals("lastActivityAt", result.sortBy());
      assertEquals(SortDirection.DESC, result.sortDirection());
      verify(userRepository, times(1)).findById(testUserId);
      verify(guidebookParticipantRepository, times(1)).searchParticipatingGuidebooks(
          testUserId, null, null, SortDirection.DESC, null, 21
      );
      verify(guidebookParticipantRepository, times(1)).countParticipatingGuidebooks(
          testUserId, null, null
      );
    }

    @Test
    @DisplayName("성공 - 키워드 검색")
    void successWithKeyword() {
      //given
      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          "가이드북1",
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      List<GuidebookParticipation> participations = Arrays.asList(testParticipation1);

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeParticipantGuidebookCursor(null)).thenReturn(null);
      when(guidebookParticipantRepository.searchParticipatingGuidebooks(
          testUserId,
          null,
          "가이드북1",
          SortDirection.DESC,
          null,
          21
      )).thenReturn(participations);
      when(guidebookParticipantRepository.countParticipatingGuidebooks(
          testUserId,
          "가이드북1",
          null
      )).thenReturn(1L);

      //when
      CursorPageResponseDto<GuidebookParticipationResponse> result =
          userService.getUserParticipation(currentUserId, testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(1L, result.totalCount());
      assertFalse(result.hasNext());
      verify(guidebookParticipantRepository, times(1)).searchParticipatingGuidebooks(
          testUserId, null, "가이드북1", SortDirection.DESC, null, 21
      );
      verify(guidebookParticipantRepository, times(1)).countParticipatingGuidebooks(
          testUserId, "가이드북1", null
      );
    }

    @Test
    @DisplayName("성공 - 완료된 가이드북 필터링")
    void successWithCompletedFilter() {
      //given
      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          null,
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.DESC,
          null,
          20,
          ParticipationFilter.COMPLETED
      );

      List<GuidebookParticipation> participations = Arrays.asList(testParticipation2);

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeParticipantGuidebookCursor(null)).thenReturn(null);
      when(guidebookParticipantRepository.searchParticipatingGuidebooks(
          testUserId,
          null,
          null,
          SortDirection.DESC,
          ParticipationFilter.COMPLETED,
          21
      )).thenReturn(participations);
      when(guidebookParticipantRepository.countParticipatingGuidebooks(
          testUserId,
          null,
          ParticipationFilter.COMPLETED
      )).thenReturn(1L);

      //when
      CursorPageResponseDto<GuidebookParticipationResponse> result =
          userService.getUserParticipation(currentUserId, testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(1L, result.totalCount());
      assertTrue(result.data().get(0).isCompleted());
      verify(guidebookParticipantRepository, times(1)).searchParticipatingGuidebooks(
          testUserId, null, null, SortDirection.DESC, ParticipationFilter.COMPLETED, 21
      );
    }

    @Test
    @DisplayName("성공 - 진행중인 가이드북 필터링")
    void successWithProgressFilter() {
      //given
      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          null,
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.DESC,
          null,
          20,
          ParticipationFilter.PROGRESS
      );

      List<GuidebookParticipation> participations = Arrays.asList(
          testParticipation1, testParticipation3
      );

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeParticipantGuidebookCursor(null)).thenReturn(null);
      when(guidebookParticipantRepository.searchParticipatingGuidebooks(
          testUserId,
          null,
          null,
          SortDirection.DESC,
          ParticipationFilter.PROGRESS,
          21
      )).thenReturn(participations);
      when(guidebookParticipantRepository.countParticipatingGuidebooks(
          testUserId,
          null,
          ParticipationFilter.PROGRESS
      )).thenReturn(2L);

      //when
      CursorPageResponseDto<GuidebookParticipationResponse> result =
          userService.getUserParticipation(currentUserId, testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(2L, result.totalCount());
      assertFalse(result.data().get(0).isCompleted());
      assertFalse(result.data().get(1).isCompleted());
      verify(guidebookParticipantRepository, times(1)).searchParticipatingGuidebooks(
          testUserId, null, null, SortDirection.DESC, ParticipationFilter.PROGRESS, 21
      );
    }

    @Test
    @DisplayName("성공 - hasNext가 true인 경우")
    void successWithHasNext() {
      //given
      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          null,
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.DESC,
          null,
          2,
          null
      );

      List<GuidebookParticipation> participations = new ArrayList<>(Arrays.asList(
          testParticipation1, testParticipation2, testParticipation3
      ));

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeParticipantGuidebookCursor(null)).thenReturn(null);
      when(cursorCodecUtil.encodeNextCursor(
          any(GuidebookParticipationResponse.class),
          any(GuidebookParticipationSortBy.class))
      ).thenReturn("encodedCursor");
      when(guidebookParticipantRepository.searchParticipatingGuidebooks(
          testUserId,
          null,
          null,
          SortDirection.DESC,
          null,
          3
      )).thenReturn(participations);
      when(guidebookParticipantRepository.countParticipatingGuidebooks(
          testUserId,
          null,
          null
      )).thenReturn(10L);

      //when
      CursorPageResponseDto<GuidebookParticipationResponse> result =
          userService.getUserParticipation(currentUserId, testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(10L, result.totalCount());
      assertTrue(result.hasNext());
      assertNotNull(result.nextCursor());
    }

    @Test
    @DisplayName("성공 - 빈 결과")
    void successWithEmptyResult() {
      //given
      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          null,
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeParticipantGuidebookCursor(null)).thenReturn(null);
      when(guidebookParticipantRepository.searchParticipatingGuidebooks(
          testUserId,
          null,
          null,
          SortDirection.DESC,
          null,
          21
      )).thenReturn(Arrays.asList());
      when(guidebookParticipantRepository.countParticipatingGuidebooks(
          testUserId,
          null,
          null
      )).thenReturn(0L);

      //when
      CursorPageResponseDto<GuidebookParticipationResponse> result =
          userService.getUserParticipation(currentUserId, testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(0, result.size());
      assertEquals(0L, result.totalCount());
      assertFalse(result.hasNext());
      assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("성공 - 오름차순 정렬")
    void successWithAscendingOrder() {
      //given
      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          null,
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.ASC,
          null,
          20,
          null
      );

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeParticipantGuidebookCursor(null)).thenReturn(null);
      when(guidebookParticipantRepository.searchParticipatingGuidebooks(
          testUserId,
          null,
          null,
          SortDirection.ASC,
          null,
          21
      )).thenReturn(Arrays.asList());
      when(guidebookParticipantRepository.countParticipatingGuidebooks(
          testUserId,
          null,
          null
      )).thenReturn(0L);

      //when
      CursorPageResponseDto<GuidebookParticipationResponse> result =
          userService.getUserParticipation(currentUserId, testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(SortDirection.ASC, result.sortDirection());
      verify(guidebookParticipantRepository, times(1)).searchParticipatingGuidebooks(
          testUserId, null, null, SortDirection.ASC, null, 21
      );
    }

    @Test
    @DisplayName("성공 - 커서 기반 페이징")
    void successWithCursor() {
      //given
      String cursorString = "encodedCursor";
      GuidebookParticipationCursor cursor = mock(GuidebookParticipationCursor.class);

      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          null,
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.DESC,
          cursorString,
          20,
          null
      );

      List<GuidebookParticipation> participations = Arrays.asList(testParticipation2);

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeParticipantGuidebookCursor(cursorString)).thenReturn(cursor);
      when(guidebookParticipantRepository.searchParticipatingGuidebooks(
          testUserId,
          cursor,
          null,
          SortDirection.DESC,
          null,
          21
      )).thenReturn(participations);
      when(guidebookParticipantRepository.countParticipatingGuidebooks(
          testUserId,
          null,
          null
      )).thenReturn(2L);

      //when
      CursorPageResponseDto<GuidebookParticipationResponse> result =
          userService.getUserParticipation(currentUserId, testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(1, result.size());
      verify(cursorCodecUtil, times(1)).decodeParticipantGuidebookCursor(cursorString);
      verify(guidebookParticipantRepository, times(1)).searchParticipatingGuidebooks(
          testUserId, cursor, null, SortDirection.DESC, null, 21
      );
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자")
    void failsWhenUserNotFound() {
      //given
      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          null,
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class,
          () -> userService.getUserParticipation(currentUserId, testUserId, request));
      verify(userRepository, times(1)).findById(testUserId);
      verify(guidebookParticipantRepository, never()).searchParticipatingGuidebooks(
          any(UUID.class), any(), any(), any(SortDirection.class), any(), any(Integer.class)
      );
    }

    @Test
    @DisplayName("실패 - 탈퇴한 사용자의 참여 가이드북 조회")
    void failsWhenUserIsDeleted() {
      //given
      User deletedUser = User.builder()
          .nickname("탈퇴한유저")
          .email("deleted@test.com")
          .password("password")
          .build();
      deletedUser.softDelete();

      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          null,
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(deletedUser));

      //when & then
      assertThrows(UserNotFoundException.class,
          () -> userService.getUserParticipation(currentUserId, testUserId, request));
      verify(userRepository, times(1)).findById(testUserId);
      verify(guidebookParticipantRepository, never()).searchParticipatingGuidebooks(
          any(UUID.class), any(), any(), any(SortDirection.class), any(), any(Integer.class)
      );
    }

    @Test
    @DisplayName("성공 - 응답 데이터에 가이드북 정보와 참여 정보가 포함됨")
    void successWithResponseData() {
      //given
      GuidebookParticipationSearchRequest request = new GuidebookParticipationSearchRequest(
          null,
          GuidebookParticipationSortBy.LAST_ACTIVITY_AT,
          SortDirection.DESC,
          null,
          20,
          null
      );

      List<GuidebookParticipation> participations = Arrays.asList(testParticipation1);

      GuidebookResponse mockGuidebookResponse = GuidebookResponse.builder()
          .gid(testGuidebook1.getId())
          .title(testGuidebook1.getTitle())
          .description(testGuidebook1.getDescription())
          .emoji(testGuidebook1.getEmoji())
          .color(testGuidebook1.getColor())
          .build();

      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(cursorCodecUtil.decodeParticipantGuidebookCursor(null)).thenReturn(null);
      when(guidebookMapper.toResponse(testGuidebook1)).thenReturn(mockGuidebookResponse);
      when(guidebookParticipantRepository.searchParticipatingGuidebooks(
          testUserId,
          null,
          null,
          SortDirection.DESC,
          null,
          21
      )).thenReturn(participations);
      when(guidebookParticipantRepository.countParticipatingGuidebooks(
          testUserId,
          null,
          null
      )).thenReturn(1L);

      //when
      CursorPageResponseDto<GuidebookParticipationResponse> result =
          userService.getUserParticipation(currentUserId, testUserId, request);

      //then
      assertNotNull(result);
      assertEquals(1, result.size());

      GuidebookParticipationResponse response = result.data().get(0);
      assertNotNull(response.guidebookResponse());
      assertNotNull(response.lastActivityAt());
      assertNotNull(response.isCompleted());
      assertEquals(false, response.isCompleted());
    }
  }

  @Nested
  @DisplayName("사용자 탈퇴")
  class DeleteUserTest {

    @Test
    @DisplayName("성공 - 정상적으로 사용자 탈퇴")
    void success() {
      //given
      UUID userId = UUID.randomUUID();
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      //when
      userService.delete(userId);

      //then
      assertTrue(user.isDeleted());
      verify(userRepository, times(1)).findById(userId);
      verify(refreshTokenService, times(1)).delete(user);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자")
    void failsWhenUserNotFound() {
      //given
      UUID userId = UUID.randomUUID();

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class, () -> userService.delete(userId));
      verify(userRepository, times(1)).findById(userId);
      verify(refreshTokenService, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("실패 - 이미 탈퇴한 사용자")
    void failsWhenUserAlreadyDeleted() {
      //given
      UUID userId = UUID.randomUUID();
      User user = User.builder()
          .nickname("테스트유저")
          .email("test@test.com")
          .password("password")
          .build();
      user.softDelete();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      //when & then
      assertThrows(UserAlreadyDeletedException.class, () -> userService.delete(userId));
      verify(userRepository, times(1)).findById(userId);
      verify(refreshTokenService, never()).delete(any(User.class));
    }
  }

  @Nested
  @DisplayName("사용자 복구")
  class RestoreUserTest {

    @Test
    @DisplayName("성공 - 정상적으로 사용자 복구")
    void success() {
      //given
      String email = "test@test.com";
      String password = "password";
      User user = User.builder()
          .nickname("테스트유저")
          .email(email)
          .password("encodedPassword")
          .build();
      user.softDelete();

      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);

      //when
      userService.restore(email, password);

      //then
      assertFalse(user.isDeleted());
      assertNull(user.getDeletedAt());
      verify(userRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, times(1)).matches(password, "encodedPassword");
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자")
    void failsWhenUserNotFound() {
      //given
      String email = "test@test.com";
      String password = "password";

      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class, () -> userService.restore(email, password));
      verify(userRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("실패 - 비밀번호 불일치")
    void failsWhenPasswordMismatch() {
      //given
      String email = "test@test.com";
      String password = "wrongPassword";
      User user = User.builder()
          .nickname("테스트유저")
          .email(email)
          .password("encodedPassword")
          .build();
      user.softDelete();

      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(false);

      //when & then
      assertThrows(UserNotFoundException.class, () -> userService.restore(email, password));
      assertTrue(user.isDeleted()); // 복구되지 않아야 함
      verify(userRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, times(1)).matches(password, "encodedPassword");
    }

    @Test
    @DisplayName("실패 - 복구 기간 만료 (30일 초과)")
    void failsWhenRestorePeriodExpired() {
      //given
      String email = "test@test.com";
      String password = "password";
      User user = mock(User.class);

      when(user.isDeleted()).thenReturn(true);
      when(user.getPassword()).thenReturn("encodedPassword");
      when(user.getDeletedAt()).thenReturn(LocalDateTime.now().minusDays(31));
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);

      //when & then
      assertThrows(UserRestorePeriodExpiredException.class,
          () -> userService.restore(email, password));
      verify(userRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, times(1)).matches(password, "encodedPassword");
      verify(user, never()).restore();
    }

    @Test
    @DisplayName("성공 - 탈퇴하지 않은 사용자 복구 시도 (아무 작업 안함)")
    void successWhenUserNotDeleted() {
      //given
      String email = "test@test.com";
      String password = "password";
      User user = User.builder()
          .nickname("테스트유저")
          .email(email)
          .password("encodedPassword")
          .build();
      // softDelete 호출하지 않음 - 탈퇴하지 않은 상태

      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

      //when
      userService.restore(email, password);

      //then
      assertFalse(user.isDeleted());
      verify(userRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, never()).matches(any(), any()); // 비밀번호 검증하지 않음
    }

    @Test
    @DisplayName("성공 - 복구 기간 경계값 (30일 이내)")
    void successWhenWithinRestorePeriod() {
      //given
      String email = "test@test.com";
      String password = "password";
      User user = mock(User.class);

      when(user.isDeleted()).thenReturn(true);
      when(user.getPassword()).thenReturn("encodedPassword");
      when(user.getDeletedAt()).thenReturn(LocalDateTime.now().minusDays(29));
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);

      //when
      userService.restore(email, password);

      //then
      verify(user, times(1)).restore();
    }
  }
}
