package region.jidogam.domain.guidebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GuidebookAddPlaceRequest(

    @Schema(description = "장소ID")
    @NotNull(message = "장소ID는 필수입니다.")
    UUID pid,

    @Schema(description = "지도 이미지 key 값")
    String mapImageUrl

) {

}
