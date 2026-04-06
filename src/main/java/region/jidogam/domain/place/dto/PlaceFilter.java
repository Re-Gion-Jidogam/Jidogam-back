package region.jidogam.domain.place.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PlaceFilter {
  NONE("none"),
  VISITED("visited"),
  NOT_VISITED("notVisited");

  private final String value;

  PlaceFilter(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static PlaceFilter from(String value) {
    for (PlaceFilter type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid filter: " + value);
  }
}
