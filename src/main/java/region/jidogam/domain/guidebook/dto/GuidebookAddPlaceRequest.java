package region.jidogam.domain.guidebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.UUID;
import org.hibernate.validator.constraints.URL;
import region.jidogam.domain.place.dto.PlaceCreateRequest;

public record GuidebookAddPlaceRequest(

  @Schema(description = "장소ID")
  UUID pid,

  @Schema(description = "장소데이터")
  @Valid
  PlaceCreateRequest place,

  @Schema(description = "지도 이미지")
  @URL(message = "올바른 url 형식이 아닙니다.")
  String mapImageUrl

) {

}
