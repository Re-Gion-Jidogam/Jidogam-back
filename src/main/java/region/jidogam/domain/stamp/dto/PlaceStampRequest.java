package region.jidogam.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PlaceStampRequest(

  @Schema(description = "장소ID", example = "uuid-id")
  @NotNull(message = "장소ID는 필수입니다.")
  UUID pid

) {

}
