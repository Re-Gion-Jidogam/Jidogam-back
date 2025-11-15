package region.jidogam.domain.user.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;

@Builder
public record GuidebookParticipationResponse(

    @Schema(description = "참여중인 가이드북")
    GuidebookResponse guidebookResponse,

    @Schema(description = "마지막 활동일")
    LocalDateTime lastActivityAt,

    @Schema(description = "가이드북 완료 여부")
    Boolean isCompleted

){
}
