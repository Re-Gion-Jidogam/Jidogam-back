package region.jidogam.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GuidebookParticipationSortBy {
  LAST_ACTIVITY_AT("lastActivityAt");

  private final String value;

  GuidebookParticipationSortBy(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static GuidebookParticipationSortBy from(String value) {
    for (GuidebookParticipationSortBy type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid sortBy: " + value);
  }
}
