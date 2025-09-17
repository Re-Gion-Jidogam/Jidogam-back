package region.jidogam.domain.user.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record UserDto(
    String nickname,
    Integer level,
    String profileUrl,
    LocalDateTime lastStampedDate
) {

}
