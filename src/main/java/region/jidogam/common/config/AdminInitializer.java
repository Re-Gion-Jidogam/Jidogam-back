package region.jidogam.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.entity.User.Role;
import region.jidogam.domain.user.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${jidogam.admin.email}")
  private String adminEmail;

  @Value("${jidogam.admin.password}")
  private String adminPassword;

  @Value("${jidogam.admin.nickname}")
  private String adminNickname;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (userRepository.existsByEmail(adminEmail)) {
      log.info("관리자 계정이 이미 존재합니다: {}", adminEmail);
      return;
    }

    User admin = User.builder()
        .email(adminEmail)
        .password(passwordEncoder.encode(adminPassword))
        .nickname(adminNickname)
        .role(Role.ADMIN)
        .build();

    userRepository.save(admin);
    log.info("관리자 계정 생성 완료: {}", adminEmail);
  }
}
