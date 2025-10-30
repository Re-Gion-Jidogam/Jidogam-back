package region.jidogam.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

    @NotBlank
    @Size(min= 3, max=20)
    @Schema(description = "변경할 사용자 닉네임")
    String nickname,

    @NotBlank
    @Size(min= 8)
    @Schema(description = "변경할 사용자 비밀번호")
    String password,

    String profileImageUrl

) {

}
