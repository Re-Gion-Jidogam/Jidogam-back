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
  @DisplayName("getUsers 메서드")
  class GetUsers {

    @Test
    @DisplayName("검색 조건으로 사용자 목록을 조회한다")
    void returnsPagedUsers() {
      // given
      AdminUserSearchRequest request = AdminUserSearchRequest.of(null, null, null, 0, 20);
      User user = createUser("test@test.com", "tester", Role.USER);
      Page<User> userPage = new PageImpl<>(List.of(user));

      when(adminUserRepository.searchUsers(any(), any(), any(), any(Pageable.class)))
          .thenReturn(userPage);

      // when
      Page<AdminUserResponse> result = adminUserService.getUsers(request);

      // then
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).email()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("키워드로 필터링하여 조회한다")
    void filtersWithKeyword() {
      // given
      AdminUserSearchRequest request = AdminUserSearchRequest.of("test", null, null, 0, 20);
      Page<User> emptyPage = new PageImpl<>(List.of());

      when(adminUserRepository.searchUsers(eq("test"), any(), any(), any(Pageable.class)))
          .thenReturn(emptyPage);

      // when
      Page<AdminUserResponse> result = adminUserService.getUsers(request);

      // then
      assertThat(result.getContent()).isEmpty();
      verify(adminUserRepository).searchUsers(eq("test"), any(), any(), any(Pageable.class));
    }
  }

  @Nested
  @DisplayName("getUser 메서드")
  class GetUser {

    @Test
    @DisplayName("사용자 ID로 상세 정보를 조회한다")
    void returnsUserDetail() {
      // given
      UUID userId = UUID.randomUUID();
      User user = createUser("test@test.com", "tester", Role.USER);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      // when
      AdminUserResponse result = adminUserService.getUser(userId);

      // then
      assertThat(result.email()).isEqualTo("test@test.com");
      assertThat(result.nickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외가 발생한다")
    void throwsWhenUserNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> adminUserService.getUser(userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("updateUser 메서드")
  class UpdateUser {

    @Test
    @DisplayName("닉네임을 변경한다")
    void updatesNickname() {
      // given
      UUID userId = UUID.randomUUID();
      User user = createUser("test@test.com", "oldNick", Role.USER);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userRepository.existsByNickname("newNick")).thenReturn(false);

      AdminUserUpdateRequest request = new AdminUserUpdateRequest("newNick", null);

      // when
      AdminUserResponse result = adminUserService.updateUser(userId, request);

      // then
      assertThat(result.nickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("역할을 변경한다")
    void updatesRole() {
      // given
      UUID userId = UUID.randomUUID();
      User user = createUser("test@test.com", "tester", Role.USER);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      AdminUserUpdateRequest request = new AdminUserUpdateRequest(null, Role.ADMIN);

      // when
      AdminUserResponse result = adminUserService.updateUser(userId, request);

      // then
      assertThat(result.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("중복 닉네임으로 변경 시 예외가 발생한다")
    void throwsOnDuplicateNickname() {
      // given
      UUID userId = UUID.randomUUID();
      User user = createUser("test@test.com", "oldNick", Role.USER);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(userRepository.existsByNickname("dupNick")).thenReturn(true);

      AdminUserUpdateRequest request = new AdminUserUpdateRequest("dupNick", null);

      // when & then
      assertThatThrownBy(() -> adminUserService.updateUser(userId, request))
          .isInstanceOf(UserNicknameConflictException.class);
    }
  }

  @Nested
  @DisplayName("deleteUser 메서드")
  class DeleteUser {

    @Test
    @DisplayName("사용자를 소프트 삭제한다")
    void softDeletesUser() {
      // given
      UUID userId = UUID.randomUUID();
      User user = createUser("test@test.com", "tester", Role.USER);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      // when
      adminUserService.deleteUser(userId);

      // then
      assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 사용자 삭제 시 예외가 발생한다")
    void throwsWhenUserNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> adminUserService.deleteUser(userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("restoreUser 메서드")
  class RestoreUser {

    @Test
    @DisplayName("삭제된 사용자를 복구한다")
    void restoresDeletedUser() {
      // given
      UUID userId = UUID.randomUUID();
      User user = createUser("test@test.com", "tester", Role.USER);
      user.softDelete();
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      // when
      adminUserService.restoreUser(userId);

      // then
      assertThat(user.isDeleted()).isFalse();
    }
  }
}