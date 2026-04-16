package region.jidogam.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import region.jidogam.domain.admin.dto.AdminUserResponse;
import region.jidogam.domain.admin.dto.AdminUserSearchRequest;
import region.jidogam.domain.admin.dto.AdminUserUpdateRequest;
import region.jidogam.domain.admin.repository.AdminUserRepository;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.entity.User.Role;
import region.jidogam.domain.user.exception.UserNicknameConflictException;
import region.jidogam.domain.user.exception.UserNotFoundException;
import region.jidogam.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService 테스트")
class AdminUserServiceTest {

  @InjectMocks
  private AdminUserService adminUserService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AdminUserRepository adminUserRepository;

  private User createUser(String email, String nickname, Role role) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .password("encoded_password")
        .role(role)
        .build();
  }

  @Nested
  @DisplayName("getUsers")
  class GetUsers {

    @Test
    @DisplayName("검색 조건으로 사용자 목록을 조회한다")
    void returnsPagedUsers() {
      AdminUserSearchRequest request = AdminUserSearchRequest.of(null, null, null, 0, 20);
      User user = createUser("test@test.com", "tester", Role.USER);
      Page<User> userPage = new PageImpl<>(List.of(user));

      when(adminUserRepository.searchUsers(any(), any(), any(), any(Pageable.class)))
          .thenReturn(userPage);

      Page<AdminUserResponse> result = adminUserService.getUsers(request);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).email()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("키워드로 필터링하여 조회한다")
    void filtersWithKeyword() {
      AdminUserSearchRequest request = AdminUserSearchRequest.of("test", null, null, 0, 20);
      Page<User> emptyPage = new PageImpl<>(List.of());

      when(adminUserRepository.searchUsers(eq("test"), any(), any(), any(Pageable.class)))
          .thenReturn(emptyPage);

      Page<AdminUserResponse> result = adminUserService.getUsers(request);

      assertThat(result.getContent()).isEmpty();
      verify(adminUserRepository).searchUsers(eq("test"), any(), any(), any(Pageable.class));
    }
  }

  @Nested
  @DisplayName("getUser")
  class GetUser {

    @Test
    @DisplayName("사용자 ID로 상세 정보를 조회한다")
    void returnsUserDetail() {
      UUID userId = UUID.randomUUID();
      User user = createUser("test@test.com", "tester", Role.USER);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      AdminUserResponse result = adminUserService.getUser(userId);

      assertThat(result.email()).isEqualTo("test@test.com");
      assertThat(result.nickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외가 발생한다")
    void throwsWhenUserNotFound() {
      UUID userId = UUID.randomUUID();
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminUserService.getUser(userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("updateUser")
  class UpdateUser {

    @Test
    @DisplayName("닉네임을 변경한다")
    void updatesNickname() {
      UUID userId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      User user = createUser("test@test.com", "oldNick", Role.USER);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userRepository.existsByNickname("newNick")).thenReturn(false);

      AdminUserUpdateRequest request = new AdminUserUpdateRequest("newNick", null);

      AdminUserResponse result = adminUserService.updateUser(userId, request, adminId);

      assertThat(result.nickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("역할을 변경한다")
    void updatesRole() {
      UUID userId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      User user = createUser("test@test.com", "tester", Role.USER);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      AdminUserUpdateRequest request = new AdminUserUpdateRequest(null, Role.ADMIN);

      AdminUserResponse result = adminUserService.updateUser(userId, request, adminId);

      assertThat(result.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("중복 닉네임으로 변경 시 예외가 발생한다")
    void throwsOnDuplicateNickname() {
      UUID userId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      User user = createUser("test@test.com", "oldNick", Role.USER);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userRepository.existsByNickname("dupNick")).thenReturn(true);

      AdminUserUpdateRequest request = new AdminUserUpdateRequest("dupNick", null);

      assertThatThrownBy(() -> adminUserService.updateUser(userId, request, adminId))
          .isInstanceOf(UserNicknameConflictException.class);
    }

    @Test
    @DisplayName("자기 자신의 역할은 변경할 수 없다")
    void throwsOnSelfRoleChange() {
      UUID userId = UUID.randomUUID();
      User user = createUser("admin@test.com", "admin", Role.ADMIN);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      AdminUserUpdateRequest request = new AdminUserUpdateRequest(null, Role.USER);

      assertThatThrownBy(() -> adminUserService.updateUser(userId, request, userId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("자기 자신");
    }
  }

  @Nested
  @DisplayName("deleteUser")
  class DeleteUser {

    @Test
    @DisplayName("사용자를 소프트 삭제한다")
    void softDeletesUser() {
      UUID userId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      User user = createUser("test@test.com", "tester", Role.USER);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      adminUserService.deleteUser(userId, adminId);

      assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 사용자 삭제 시 예외가 발생한다")
    void throwsWhenUserNotFound() {
      UUID userId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> adminUserService.deleteUser(userId, adminId))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("자기 자신을 삭제할 수 없다")
    void throwsOnSelfDelete() {
      UUID userId = UUID.randomUUID();

      assertThatThrownBy(() -> adminUserService.deleteUser(userId, userId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("자기 자신");
    }
  }

  @Nested
  @DisplayName("restoreUser")
  class RestoreUser {

    @Test
    @DisplayName("삭제된 사용자를 복구한다")
    void restoresDeletedUser() {
      UUID userId = UUID.randomUUID();
      User user = createUser("test@test.com", "tester", Role.USER);
      user.softDelete();
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      adminUserService.restoreUser(userId);

      assertThat(user.isDeleted()).isFalse();
    }
  }
}
