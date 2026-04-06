package region.jidogam.common.dto;

public record Cursor(
    String lastValue,
    String lastId
) {
}
