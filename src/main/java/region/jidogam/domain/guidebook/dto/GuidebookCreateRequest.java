package region.jidogam.domain.guidebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GuidebookCreateRequest(

  @Schema(description = "가이드북 제목")
  @NotNull(message = "가이드북 제목은 필수입니다.")
  @NotBlank(message = "가이드북 제목은 빈 값일 수 없습니다.")
  @Size(min = 2, max = 20, message = "가이드북 제목은 2자 이상 20자 이하여야 합니다.")
  String title,

  @Schema(description = "가이드북 설명")
//  @NotBlank(message = "가이드북 설명은 빈 값일 수 없습니다.")
  String description,

  @Schema(description = "가이드북 배경 이모지")
//  @NotBlank(message = "가이드북 이모지는 빈 값일 수 없습니다.")
  String emoji,

  @Schema(description = "가이드북 배경 컬러")
//  @NotBlank(message = "가이드북 컬러는 빈 값일 수 없습니다.")
  String color,

  @Schema(description = "가이드북 썸네일 url")
//  @NotBlank(message = "가이드북 썸네일 url은 빈 값일 수 없습니다.")
  String thumbnail
) {

}
