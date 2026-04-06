package region.jidogam.domain.auth.dto;

import java.time.LocalDateTime;

public record LoginResponse(
    String accessToken,
    LocalDateTime lastStampedAt
) {

}
