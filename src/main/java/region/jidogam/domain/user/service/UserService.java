package region.jidogam.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.infrastructure.jwt.JwtProvider;
import region.jidogam.infrastructure.jwt.RefreshTokenService;
import region.jidogam.infrastructure.jwt.dto.TokenPair;
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserEmailConflictException;
import region.jidogam.domain.user.exception.UserNicknameConflictException;
import region.jidogam.domain.user.exception.UserNicknameLengthException;
import region.jidogam.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public TokenPair create(UserCreateRequest request){
    log.info("유저 생성 시작: nickname = {}, email = {}", request.nickname(), request.email());
    if(userRepository.existsByNickname(request.nickname())){
      throw UserNicknameConflictException.withNickname(request.nickname());
    }
    if(userRepository.existsByEmail(request.email())){
      throw UserEmailConflictException.withEmail(request.email());
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
  public void validateNickname(String nickname){
    if(nickname == null || nickname.isBlank() || nickname.length() < 2 || nickname.length() > 20){
      throw UserNicknameLengthException.withNickname(nickname);
    }
    if (userRepository.existsByNickname(nickname)){
      throw UserNicknameConflictException.withNickname(nickname);
    }
  }
}
