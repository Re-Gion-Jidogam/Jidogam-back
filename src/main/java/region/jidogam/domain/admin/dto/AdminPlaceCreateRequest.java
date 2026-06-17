package region.jidogam.domain.admin.dto;

import java.math.BigDecimal;

public record AdminPlaceCreateRequest(
    String kakaoId,
    String name,
    String address,
    String category,
    BigDecimal x,
    BigDecimal y
) {

}
