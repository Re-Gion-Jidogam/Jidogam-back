package region.jidogam.domain.auth.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record LoginResult(
    String accessToken,
    String refreshToken,
    LocalDateTime lastStampedAt
) {

}
