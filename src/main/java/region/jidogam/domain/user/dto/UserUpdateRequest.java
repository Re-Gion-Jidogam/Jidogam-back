package region.jidogam.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Optional;

public record UserUpdateRequest(

    @NotBlank
    @Size(min = 3, max = 20, message = "사용자 닉네임은 3 ~ 20글자여야 합니다.")
    @Schema(description = "변경할 사용자 닉네임")
    Optional<String> nickname,

    @NotBlank
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @Schema(description = "변경할 사용자 비밀번호")
    Optional<String> password,

    Optional<String> profileImageUrl

) {

}
