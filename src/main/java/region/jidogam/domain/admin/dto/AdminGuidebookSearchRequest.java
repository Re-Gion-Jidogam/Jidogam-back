package region.jidogam.domain.admin.dto;

public record AdminGuidebookSearchRequest(
    String keyword,
    Boolean isPublished,
    int page,
    int size
) {

  public AdminGuidebookSearchRequest {
    if (page < 0) {
      page = 0;
    }
    if (size <= 0 || size > 100) {
      size = 20;
    }
  }

  public static AdminGuidebookSearchRequest of(String keyword, Boolean isPublished,
      Integer page, Integer size) {
    return new AdminGuidebookSearchRequest(
        keyword,
        isPublished,
        page == null ? 0 : page,
        size == null ? 20 : size
    );
  }
}
