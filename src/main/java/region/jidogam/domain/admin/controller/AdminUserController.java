package region.jidogam.domain.admin.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import region.jidogam.domain.admin.dto.AdminUserResponse;
import region.jidogam.domain.admin.dto.AdminUserSearchRequest;
import region.jidogam.domain.admin.dto.AdminUserUpdateRequest;
import region.jidogam.domain.admin.service.AdminUserService;
import region.jidogam.domain.user.entity.User;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping
  public String userList(
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "role", required = false) User.Role role,
      @RequestParam(value = "deleted", required = false) Boolean deleted,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size,
      Model model) {

    AdminUserSearchRequest request = AdminUserSearchRequest.of(keyword, role, deleted, page, size);
    Page<AdminUserResponse> users = adminUserService.getUsers(request);

    model.addAttribute("users", users);
    model.addAttribute("keyword", keyword);
    model.addAttribute("role", role);
    model.addAttribute("deleted", deleted);

    return "admin/users/list";
  }

  @GetMapping("/{userId}")
  public String userDetail(@PathVariable UUID userId, Model model) {
    AdminUserResponse user = adminUserService.getUser(userId);
    model.addAttribute("user", user);
    return "admin/users/detail";
  }

  @GetMapping("/{userId}/edit")
  public String userEditForm(@PathVariable UUID userId, Model model) {
    AdminUserResponse user = adminUserService.getUser(userId);
    model.addAttribute("user", user);
    return "admin/users/edit";
  }

  @PostMapping("/{userId}/edit")
  public String userUpdate(
      @PathVariable UUID userId,
      @RequestParam(value = "nickname", required = false) String nickname,
      @RequestParam(value = "role", required = false) User.Role role,
      RedirectAttributes redirectAttributes) {

    AdminUserUpdateRequest request = new AdminUserUpdateRequest(nickname, role);
    adminUserService.updateUser(userId, request);
    redirectAttributes.addFlashAttribute("successMessage", "사용자 정보가 수정되었습니다.");
    return "redirect:/admin/users/" + userId;
  }

  @PostMapping("/{userId}/delete")
  public String userDelete(@PathVariable UUID userId, RedirectAttributes redirectAttributes) {
    adminUserService.deleteUser(userId);
    redirectAttributes.addFlashAttribute("successMessage", "사용자가 삭제되었습니다.");
    return "redirect:/admin/users/" + userId;
  }

  @PostMapping("/{userId}/restore")
  public String userRestore(@PathVariable UUID userId, RedirectAttributes redirectAttributes) {
    adminUserService.restoreUser(userId);
    redirectAttributes.addFlashAttribute("successMessage", "사용자가 복구되었습니다.");
    return "redirect:/admin/users/" + userId;
  }
}
