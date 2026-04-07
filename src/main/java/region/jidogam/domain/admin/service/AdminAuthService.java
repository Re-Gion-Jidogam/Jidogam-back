package region.jidogam.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.admin.dto.AdminLoginRequest;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.entity.User.Role;
import region.jidogam.domain.user.repository.UserRepository;
import region.jidogam.infrastructure.jwt.JwtProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;

  @Transactional(readOnly = true)
  public String login(AdminLoginRequest request) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    if (user.getRole() != Role.ADMIN) {
      throw new IllegalArgumentException("관리자 권한이 없습니다.");
    }

    if (user.isDeleted()) {
      throw new IllegalArgumentException("삭제된 계정입니다.");
    }

    log.info("관리자 로그인: email = {}", request.email());
    return jwtProvider.generateAccessToken(user);
  }
}
