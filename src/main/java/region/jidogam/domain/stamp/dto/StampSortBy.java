package region.jidogam.domain.stamp.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StampSortBy {

  CREATED_AT("createdAt");

  private final String value;

  StampSortBy(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static StampSortBy from(String value) {
    for (StampSortBy type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid sortBy: " + value);
  }
}
