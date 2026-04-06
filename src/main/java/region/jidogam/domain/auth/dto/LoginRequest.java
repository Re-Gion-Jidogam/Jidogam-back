package region.jidogam.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @Email(message = "이메일로 로그인 가능합니다.")
    String email,
    @Size(min = 8, max = 30)
    String password
) {

}
