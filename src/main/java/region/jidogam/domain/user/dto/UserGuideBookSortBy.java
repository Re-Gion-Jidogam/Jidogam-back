package region.jidogam.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserGuideBookSortBy {
  CREATED_AT("createdAt"),
  UPDATED_AT("updatedAt");

  private final String value;

  UserGuideBookSortBy(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static UserGuideBookSortBy from(String value) {
    for (UserGuideBookSortBy type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid sortBy: " + value);
  }
}