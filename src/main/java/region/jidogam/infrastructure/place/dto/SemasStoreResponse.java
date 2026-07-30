package region.jidogam.infrastructure.place.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SemasStoreResponse(

    Header header,
    Body body
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Header(
      String resultCode,
      String resultMsg
  ) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Body(
      List<SemasStoreItem> items,
      int numOfRows,
      int pageNo,
      int totalCount
  ) {

  }
}