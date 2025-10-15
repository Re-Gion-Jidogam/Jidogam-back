package region.jidogam.domain.guidebook.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GuidebookSortBy {
  CREATED_AT("createdAt"),
  PARTICIPANT_COUNT("participantCount");

  private final String value;

  GuidebookSortBy(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static GuidebookSortBy from(String value) {
    for (GuidebookSortBy type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid sortBy: " + value);
  }
}
