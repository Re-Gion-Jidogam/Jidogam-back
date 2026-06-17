package region.jidogam.domain.admin.dto;

public record AdminPlaceSearchRequest(
    String keyword,
    Boolean deleted,
    int page,
    int size
) {

  public AdminPlaceSearchRequest {
    if (page < 0) {
      page = 0;
    }
    if (size <= 0 || size > 100) {
      size = 20;
    }
  }

  public static AdminPlaceSearchRequest of(String keyword, Boolean deleted,
      Integer page, Integer size) {
    return new AdminPlaceSearchRequest(
        keyword,
        deleted,
        page == null ? 0 : page,
        size == null ? 20 : size
    );
  }
}
