package region.jidogam.domain.guidebook.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GuidebookReviewSortBy {
  CREATED_AT("createdAt");

  private final String value;

  GuidebookReviewSortBy(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static GuidebookReviewSortBy from(String value) {
    for (GuidebookReviewSortBy type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid sortBy: " + value);
  }
}