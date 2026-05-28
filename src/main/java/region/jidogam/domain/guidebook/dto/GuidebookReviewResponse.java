package region.jidogam.domain.guidebook.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GuidebookReviewResponse(
    UUID rid,
    String content,
    Integer rating,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    AuthorDto author
) {

  public record AuthorDto(
      UUID uid,
      String nickname,
      Integer level
  ) {

  }
}