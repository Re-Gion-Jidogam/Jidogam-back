package region.jidogam.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewPasswordChangeRequest(

    @NotBlank
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    String newPassword,

    @NotBlank
    String authCode
) {
}
