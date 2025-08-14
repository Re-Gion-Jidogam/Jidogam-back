package region.jidogam.domain.area.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AreaApiResponse<T>(

  T result,
  String errCd,
  String errMsg

) {

}
