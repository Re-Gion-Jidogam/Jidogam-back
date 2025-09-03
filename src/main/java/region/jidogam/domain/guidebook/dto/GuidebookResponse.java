package region.jidogam.domain.guidebook.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GuidebookResponse(
  UUID gid,
  String title,
  String description,
  String thumbnailUrl,
  String mapImageUrl,
  String emoji,
  String color,
  int point,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  LocalDateTime publishedDate,
  double score,
  int participantCount,
  int totalPlaceCount,
  int visitedPlaceCount,
  AuthorDto author

) {

  public record AuthorDto(
    UUID uid,
    String nickname,
    Long level
  ) {

  }
}
