package region.jidogam.domain.jwt.dto;

import lombok.Builder;

@Builder
public record TokenPair(
    String accessToken,
    String refreshToken
) {

}
