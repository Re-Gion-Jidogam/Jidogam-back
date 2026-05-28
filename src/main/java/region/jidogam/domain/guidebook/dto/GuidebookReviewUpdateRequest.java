package region.jidogam.domain.guidebook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record GuidebookReviewUpdateRequest(

    @Min(value = 1, message = "최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "최대 5점까지 입력 가능합니다.")
    Integer rating,

    @Size(max = 500, message = "내용은 500자 이하로 입력해주세요.")
    String content
) {

}