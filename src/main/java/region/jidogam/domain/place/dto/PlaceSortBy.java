package region.jidogam.domain.place.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PlaceSortBy {
  CREATED_AT("createdAt"),
  DISTANCE("distance");

  private final String value;

  PlaceSortBy(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static PlaceSortBy from(String value) {
    for (PlaceSortBy type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid sortBy: " + value);
  }
}
