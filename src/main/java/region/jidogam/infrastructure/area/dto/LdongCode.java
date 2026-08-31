package region.jidogam.infrastructure.area.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LdongCode(

    @JsonProperty("name")
    String name,

    @JsonProperty("code")
    String code

) {

}
