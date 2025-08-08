package region.jidogam.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.user.dto.UserCreateRequest;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserEmailConflictException;
import region.jidogam.domain.user.exception.UserNicknameConflictException;
import region.jidogam.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void create(UserCreateRequest request){
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

    userRepository.save(user);
    log.info("유저 생성 완료: nickname = {}, email = {}", request.nickname(), request.email());
  }
}
