package region.jidogam.infrastructure.area.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiResponse<T>(

    Response<T> response

) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Response<T>(
      Header header,
      Body<T> body
  ) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Header(
      String resultCode,
      String resultMsg
  ) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Body<T>(
      Items<T> items,
      int numOfRows,
      int pageNo,
      int totalCount
  ) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Items<T>(
      List<T> item
  ) {

  }
}
