package region.jidogam.domain.guidebook.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GuidebookFilter {
  POPULAR("popular"),
  LOCAL("local"),
  IS_PUBLISHED("isPublished");

  private final String value;

  GuidebookFilter(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static GuidebookFilter from(String value) {
    for (GuidebookFilter type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid filter: " + value);
  }
}
