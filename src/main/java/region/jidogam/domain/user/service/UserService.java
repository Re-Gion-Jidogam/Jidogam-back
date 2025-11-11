package region.jidogam.domain.user.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.common.dto.response.CursorPageResponseDto;
import region.jidogam.domain.auth.entity.EmailAuthCode;
import region.jidogam.domain.auth.exception.EmailAuthNotFoundException;
import region.jidogam.domain.auth.repository.EmailAuthCodeRepository;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.mapper.GuidebookMapper;
import region.jidogam.domain.guidebook.repository.GuidebookRepository;
import region.jidogam.common.util.CursorCodecUtil;
import region.jidogam.domain.stamp.entity.Stamp;
import region.jidogam.domain.stamp.repository.StampRepository;
import region.jidogam.domain.user.exception.UserExpException;
import region.jidogam.domain.user.mapper.UserMapper;
import region.jidogam.domain.user.dto.UserDto;
import region.jidogam.domain.user.dto.UserGuidebookCursor;
import region.jidogam.domain.user.dto.UserGuidebookSearchRequest;
import region.jidogam.domain.user.dto.UserUpdateRequest;
import region.jidogam.domain.user.exception.UnverifiedEmailException;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.exception.UserPasswordLengthException;
import region.jidogam.infrastructure.jwt.JwtProvider;
import region.jidogam.infrastructure.jwt.RefreshTokenService;
import region.jidogam.infrastructure.jwt.dto.TokenPair;
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.InvalidEmailFormatException;
import region.jidogam.domain.user.exception.UserEmailConflictException;
import region.jidogam.domain.user.exception.UserNicknameConflictException;
import region.jidogam.domain.user.exception.UserNicknameLengthException;
import region.jidogam.domain.user.repository.UserRepository;
import region.jidogam.domain.user.util.LevelCalculator;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;
  private final StampRepository stampRepository;
  private final EmailAuthCodeRepository emailAuthCodeRepository;
  private final GuidebookRepository guidebookRepository;

  private final UserMapper userMapper;
  private final GuidebookMapper guidebookMapper;

  private final CursorCodecUtil cursorCodecUtil;
  private final LevelCalculator levelCalculator;

  @Transactional
  public TokenPair create(UserCreateRequest request) {
    log.info("유저 생성 시작: nickname = {}, email = {}", request.nickname(), request.email());
    if (userRepository.existsByNickname(request.nickname())) {
      throw UserNicknameConflictException.withNickname(request.nickname());
    }
    if (userRepository.existsByEmail(request.email())) {
      throw UserEmailConflictException.withEmail(request.email());
    }

    EmailAuthCode emailAuthCode = emailAuthCodeRepository.findByEmail(request.email())
        .orElseThrow(() -> EmailAuthNotFoundException.withEmail(request.email())); // 인증 내역 자체가 없음

    if (!emailAuthCode.getUsed()) {
      throw UnverifiedEmailException.withEmail(request.email()); // 인증되지 않음
    }

    User user = User.builder()
        .nickname(request.nickname())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .build();

    User savedUser = userRepository.save(user);

    String accessToken = jwtProvider.generateAccessToken(savedUser);
    String refreshToken = refreshTokenService.create(savedUser).getRefreshToken();

    log.info("유저 생성 완료: nickname = {}, email = {}", request.nickname(), request.email());

    return TokenPair.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  @Transactional(readOnly = true)
  public void validateNickname(String nickname) {
    if (nickname == null || nickname.isBlank() || nickname.length() < 2 || nickname.length() > 20) {
      throw UserNicknameLengthException.withNickname(nickname);
    }
    if (userRepository.existsByNickname(nickname)) {
      throw UserNicknameConflictException.withNickname(nickname);
    }
  }

  private void validatePassword(String password) {
    if (password.isBlank() || password.length() < 8){
      throw UserPasswordLengthException.lengthInvalid();
    }
  }

  @Transactional(readOnly = true)
  public void validateEmail(String email) {
    if (email == null || email.isBlank() || !email.matches(EMAIL_REGEX)) {
      throw InvalidEmailFormatException.withEmail(email);
    }
    if (userRepository.existsByEmail(email)) {
      throw UserEmailConflictException.withEmail(email);
    }
  }

  @Transactional(readOnly = true)
  public UserDto getUserInfo(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> UserNotFoundException.withId(id));

    Stamp lastStamp = stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(id).orElse(null);

    int level = levelCalculator.calculateLevel(user.getExp());

    return userMapper.toResponse(user, level, lastStamp);
  }

  @Transactional(readOnly = true)
  public CursorPageResponseDto<GuidebookResponse> getUserGuidebookList(UUID userId, UUID authorId,
      UserGuidebookSearchRequest request) {

    UserGuidebookCursor cursor = cursorCodecUtil.decodeUserGuidebookCursor(request.cursor());

    int limit = request.limit();

    boolean isOwner = authorId.equals(userId);

    List<Guidebook> guidebooks = guidebookRepository.searchGuidebookByAuthorId(
        authorId,
        cursor,
        request.keyword(),
        request.sortBy(),
        request.sortDirection(),
        limit + 1,
        isOwner);

    long total = guidebookRepository.countGuidebookByAuthorId(authorId, isOwner, request.keyword());

    return buildResponse(guidebooks, limit, request, total);
  }

  public void decreaseUserExp(User user, int exp) {
    if (exp < 0) {
      throw UserExpException.negativeValue();
    }

    long newExp = user.getExp() - exp;
    if (newExp < 0) {
      user.updateExp(0);
      return;
    }
    user.updateExp(newExp);
  }

  public void increaseUserExp(User user, int exp) {
    if (exp < 0) {
      throw UserExpException.negativeValue();
    }
    long newExp = user.getExp() + exp;
    user.updateExp(newExp);
  }

  // 추후 재사용을 위해 분리
  private CursorPageResponseDto<GuidebookResponse> buildResponse(List<Guidebook> guidebooks,
      int limit, UserGuidebookSearchRequest request, long total) {
    boolean hasNext = guidebooks.size() > limit;
    if (hasNext) {
      guidebooks.remove(limit);
    }

    List<GuidebookResponse> responses = guidebooks.stream()
        .map(guidebookMapper::toResponse)
        .collect(Collectors.toList());

    String nextCursor = null;
    if (hasNext) {
      nextCursor = cursorCodecUtil.encodeNextCursor(
          responses.get(responses.size() - 1),
          request.sortBy()
      );
    }

    return CursorPageResponseDto.<GuidebookResponse>builder()
        .data(responses)
        .hasNext(hasNext)
        .size(responses.size())
        .sortBy(request.sortBy().getValue())
        .sortDirection(request.sortDirection())
        .totalCount(total)
        .nextCursor(nextCursor)
        .build();
  }

  @Transactional
  public UserDto update(UUID userId, UserUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    // Patch 이므로 null이 아닐 때만 업데이트
    if (request.nickname() != null) {
      validateNickname(request.nickname()); // 공백으로 되어있거나 중복인 경우 예외 발생
      user.changeNickname(request.nickname());
    }
    if (request.password() != null) {
      validatePassword(request.password()); // 공백으로 되어있거나 중복인 경우 예외 발생
      user.changePassword(passwordEncoder.encode(request.password()));
    }
    if (request.profileImageUrl() != null) {
      user.changeProfileImage(request.profileImageUrl());
    }

    userRepository.save(user);

    // TODO
    // 도장 수가 많아질 경우 성능 우려. user에 lastStampedAt을 추가하는 방향 고려
    Stamp stamp = stampRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId).orElse(null);

    return userMapper.toResponse(user, 0, stamp);
  }
}
