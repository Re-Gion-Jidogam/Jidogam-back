package region.jidogam.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
    @Schema(description = "닉네임", example = "지역부흥단")
    @NotBlank
    String nickname,

    @Schema(description = "이메일", example = "jidogam@example.com")
    @Email
    String email,

    @Schema(description = "비밀번호", example = "jidogam1234")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    String password
) {
}
