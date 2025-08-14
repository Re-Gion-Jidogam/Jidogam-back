package region.jidogam.domain.jwt;

import lombok.Builder;

@Builder
public record TokenPair(
    String accessToken,
    String refreshToken
) {

}
