package region.jidogam.domain.guidebook.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ParticipationFilter {
  COMPLETED("completed"),
  PROGRESS("progress");

  private final String value;

  ParticipationFilter(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ParticipationFilter from(String value) {
    for (ParticipationFilter type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid participant filter: " + value);
  }
}
