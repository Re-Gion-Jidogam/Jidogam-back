package region.jidogam.domain.place.dto;

public record FieldChange(
    String oldValue,
    String newValue
) {

  public static FieldChange of(String oldValue, String newValue) {
    return new FieldChange(oldValue, newValue);
  }
}