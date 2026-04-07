package region.jidogam.domain.admin.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import region.jidogam.domain.admin.dto.AdminUserResponse;
import region.jidogam.domain.admin.dto.AdminUserSearchRequest;
import region.jidogam.domain.admin.dto.AdminUserUpdateRequest;
import region.jidogam.domain.admin.repository.AdminUserRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.exception.UserNicknameConflictException;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;
  private final AdminUserRepository adminUserRepository;

  @Transactional(readOnly = true)
  public Page<AdminUserResponse> getUsers(AdminUserSearchRequest request) {
    PageRequest pageable = PageRequest.of(request.page(), request.size());

    return adminUserRepository.searchUsers(
        request.keyword(), request.role(), request.deleted(), pageable
    ).map(AdminUserResponse::from);
  }

  @Transactional(readOnly = true)
  public AdminUserResponse getUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    return AdminUserResponse.from(user);
  }

  @Transactional
  public AdminUserResponse updateUser(UUID userId, AdminUserUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    if (request.nickname() != null && !request.nickname().equals(user.getNickname())) {
      if (userRepository.existsByNickname(request.nickname())) {
        throw UserNicknameConflictException.withNickname(request.nickname());
      }
      user.changeNickname(request.nickname());
    }

    if (request.role() != null && request.role() != user.getRole()) {
      user.changeRole(request.role());
    }

    return AdminUserResponse.from(user);
  }

  @Transactional
  public void deleteUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    if (user.isDeleted()) {
      log.warn("이미 삭제된 사용자입니다: userId = {}", userId);
      return;
    }

    user.softDelete();
    log.info("관리자에 의해 사용자 삭제: userId = {}", userId);
  }

  @Transactional
  public void restoreUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    if (!user.isDeleted()) {
      log.warn("삭제되지 않은 사용자입니다: userId = {}", userId);
      return;
    }

    user.restore();
    log.info("관리자에 의해 사용자 복구: userId = {}", userId);
  }
}
