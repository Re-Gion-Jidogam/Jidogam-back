package region.jidogam.domain.admin.dto;

public record FieldChange(
    String oldValue,
    String newValue
) {

  public static FieldChange of(String oldValue, String newValue) {
    return new FieldChange(oldValue, newValue);
  }

  public static FieldChange of(Object oldValue, Object newValue) {
    return new FieldChange(
        oldValue == null ? null : oldValue.toString(),
        newValue == null ? null : newValue.toString()
    );
  }
}
