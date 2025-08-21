package region.jidogam.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.UUID;
import region.jidogam.domain.place.dto.PlaceCreateRequest;

public record PlaceStampRequest(

  @Schema(description = "장소ID", example = "uuid-id")
  UUID pid,

  @Schema(description = "장소데이터")
  @Valid
  PlaceCreateRequest place

) {

}
