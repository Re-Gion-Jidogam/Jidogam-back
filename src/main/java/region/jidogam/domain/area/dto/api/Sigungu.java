package region.jidogam.domain.area.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Sigungu(

  @JsonProperty("addr_name")
  String addressName,

  @JsonProperty("cd")
  String code

) {

}
