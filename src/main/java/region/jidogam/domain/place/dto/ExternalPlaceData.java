package region.jidogam.domain.place.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ExternalPlaceData(

    String externalId,

    String placeName,

    String jibunAddress,

    String roadAddress,

    String sidoName,

    String sidoCode,

    String sigunguName,

    String sigunguCode,

    String categoryCode,

    String categoryName,

    BigDecimal y,

    BigDecimal x,

    LocalDateTime fetchedAt
) {

}
