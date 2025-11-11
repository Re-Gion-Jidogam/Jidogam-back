package region.jidogam.domain.guidebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record GuidebookUpdateRequest(

    @Schema(description = "가이드북 제목")
    @Size(min = 2, max = 20, message = "가이드북 제목은 2자 이상 20자 이하여야 합니다.")
    String title,

    @Schema(description = "가이드북 설명")
    String description,

    @Schema(description = "가이드북 배경 이모지")
    String emoji,

    @Schema(description = "가이드북 배경 컬러")
    String color,

    @Schema(description = "가이드북 썸네일 key 값")
    String thumbnail,

    @Schema(description = "가이드북 출판 여부")
    Boolean isPublish
) {

}
