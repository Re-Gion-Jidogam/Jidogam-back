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
import region.jidogam.domain.admin.dto.AdminGuidebookResponse;
import region.jidogam.domain.admin.dto.AdminGuidebookSearchRequest;
import region.jidogam.domain.admin.dto.AdminGuidebookUpdateRequest;
import region.jidogam.domain.admin.service.AdminGuidebookService;

@Controller
@RequestMapping("/jidogam-admin/guidebooks")
@RequiredArgsConstructor
public class AdminGuidebookController {

  private final AdminGuidebookService adminGuidebookService;

  @GetMapping
  public String guidebookList(
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "isPublished", required = false) Boolean isPublished,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size,
      Model model) {

    AdminGuidebookSearchRequest request = AdminGuidebookSearchRequest.of(
        keyword, isPublished, page, size);
    Page<AdminGuidebookResponse> guidebooks = adminGuidebookService.getGuidebooks(request);

    model.addAttribute("guidebooks", guidebooks);
    model.addAttribute("keyword", keyword);
    model.addAttribute("isPublished", isPublished);

    return "admin/guidebooks/list";
  }

  @GetMapping("/{guidebookId}")
  public String guidebookDetail(@PathVariable UUID guidebookId, Model model) {
    AdminGuidebookResponse guidebook = adminGuidebookService.getGuidebook(guidebookId);
    model.addAttribute("guidebook", guidebook);
    return "admin/guidebooks/detail";
  }

  @GetMapping("/{guidebookId}/edit")
  public String guidebookEditForm(@PathVariable UUID guidebookId, Model model) {
    AdminGuidebookResponse guidebook = adminGuidebookService.getGuidebook(guidebookId);
    model.addAttribute("guidebook", guidebook);
    return "admin/guidebooks/edit";
  }

  @PostMapping("/{guidebookId}/edit")
  public String guidebookUpdate(
      @PathVariable UUID guidebookId,
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "description", required = false) String description,
      RedirectAttributes redirectAttributes) {

    AdminGuidebookUpdateRequest request = new AdminGuidebookUpdateRequest(title, description);
    adminGuidebookService.updateGuidebook(guidebookId, request);
    redirectAttributes.addFlashAttribute("successMessage", "가이드북이 수정되었습니다.");
    return "redirect:/jidogam-admin/guidebooks/" + guidebookId;
  }

  @PostMapping("/{guidebookId}/unpublish")
  public String guidebookUnpublish(
      @PathVariable UUID guidebookId,
      RedirectAttributes redirectAttributes) {
    adminGuidebookService.unpublishGuidebook(guidebookId);
    redirectAttributes.addFlashAttribute("successMessage", "가이드북이 미출판 상태로 변경되었습니다.");
    return "redirect:/jidogam-admin/guidebooks/" + guidebookId;
  }

  @PostMapping("/{guidebookId}/delete")
  public String guidebookDelete(
      @PathVariable UUID guidebookId,
      RedirectAttributes redirectAttributes) {
    adminGuidebookService.deleteGuidebook(guidebookId);
    redirectAttributes.addFlashAttribute("successMessage", "가이드북이 삭제되었습니다.");
    return "redirect:/jidogam-admin/guidebooks";
  }
}
