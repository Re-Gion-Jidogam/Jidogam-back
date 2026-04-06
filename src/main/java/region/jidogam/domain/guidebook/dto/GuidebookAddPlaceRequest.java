package region.jidogam.domain.guidebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.UUID;
import region.jidogam.domain.place.dto.PlaceCreateRequest;

public record GuidebookAddPlaceRequest(

    @Schema(description = "장소ID")
    UUID pid,

    @Schema(description = "장소데이터")
    @Valid
    PlaceCreateRequest place,

    @Schema(description = "지도 이미지 key 값")
    String mapImageUrl

) {

}
