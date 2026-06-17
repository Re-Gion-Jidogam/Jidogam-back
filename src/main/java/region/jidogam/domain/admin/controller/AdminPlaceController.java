package region.jidogam.domain.admin.controller;

import java.math.BigDecimal;
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
import region.jidogam.common.annotation.CurrentUserId;
import region.jidogam.domain.admin.dto.AdminPlaceCreateRequest;
import region.jidogam.domain.admin.dto.AdminPlaceResponse;
import region.jidogam.domain.admin.dto.AdminPlaceSearchRequest;
import region.jidogam.domain.admin.service.AdminPlaceService;

@Controller
@RequestMapping("/jidogam-admin/places")
@RequiredArgsConstructor
public class AdminPlaceController {

  private final AdminPlaceService adminPlaceService;

  @GetMapping
  public String placeList(
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "deleted", required = false) Boolean deleted,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size,
      Model model) {

    AdminPlaceSearchRequest request = AdminPlaceSearchRequest.of(keyword, deleted, page, size);
    Page<AdminPlaceResponse> places = adminPlaceService.getPlaces(request);

    model.addAttribute("places", places);
    model.addAttribute("keyword", keyword);
    model.addAttribute("deleted", deleted);

    return "admin/places/list";
  }

  @GetMapping("/new")
  public String placeCreateForm() {
    return "admin/places/create";
  }

  @PostMapping("/new")
  public String placeCreate(
      @CurrentUserId UUID currentAdminId,
      @RequestParam(value = "kakaoId", required = false) String kakaoId,
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "address", required = false) String address,
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "x", required = false) BigDecimal x,
      @RequestParam(value = "y", required = false) BigDecimal y,
      RedirectAttributes redirectAttributes) {

    AdminPlaceCreateRequest request =
        new AdminPlaceCreateRequest(kakaoId, name, address, category, x, y);
    AdminPlaceResponse place = adminPlaceService.createPlace(request, currentAdminId);
    redirectAttributes.addFlashAttribute("successMessage", "장소가 추가되었습니다.");
    return "redirect:/jidogam-admin/places/" + place.id();
  }

  @GetMapping("/{placeId}")
  public String placeDetail(@PathVariable UUID placeId, Model model) {
    AdminPlaceResponse place = adminPlaceService.getPlace(placeId);
    model.addAttribute("place", place);
    return "admin/places/detail";
  }

  @GetMapping("/{placeId}/edit")
  public String placeEditForm(@PathVariable UUID placeId, Model model) {
    AdminPlaceResponse place = adminPlaceService.getPlace(placeId);
    model.addAttribute("place", place);
    return "admin/places/edit";
  }

  @PostMapping("/{placeId}/edit")
  public String placeUpdateExp(
      @PathVariable UUID placeId,
      @CurrentUserId UUID currentAdminId,
      @RequestParam(value = "exp", required = false) Integer exp,
      RedirectAttributes redirectAttributes) {

    adminPlaceService.updatePlaceExp(placeId, exp, currentAdminId);
    redirectAttributes.addFlashAttribute("successMessage", "장소 포인트가 수정되었습니다.");
    return "redirect:/jidogam-admin/places/" + placeId;
  }

  @PostMapping("/{placeId}/delete")
  public String placeDelete(
      @PathVariable UUID placeId,
      @CurrentUserId UUID currentAdminId,
      RedirectAttributes redirectAttributes) {
    adminPlaceService.deletePlace(placeId, currentAdminId);
    redirectAttributes.addFlashAttribute("successMessage", "장소가 삭제되었습니다.");
    return "redirect:/jidogam-admin/places/" + placeId;
  }

  @PostMapping("/{placeId}/restore")
  public String placeRestore(
      @PathVariable UUID placeId,
      @CurrentUserId UUID currentAdminId,
      RedirectAttributes redirectAttributes) {
    adminPlaceService.restorePlace(placeId, currentAdminId);
    redirectAttributes.addFlashAttribute("successMessage", "장소가 복구되었습니다.");
    return "redirect:/jidogam-admin/places/" + placeId;
  }
}
