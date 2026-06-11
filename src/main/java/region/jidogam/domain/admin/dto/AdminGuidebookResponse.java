package region.jidogam.domain.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import region.jidogam.domain.guidebook.entity.Guidebook;

@Builder
public record AdminGuidebookResponse(
    UUID id,
    String title,
    String description,
    String emoji,
    String color,
    String thumbnailUrl,
    Boolean isPublished,
    LocalDateTime publishedDate,
    Integer participantCount,
    Integer totalPlaceCount,
    Integer exp,
    Integer ratingCount,
    double averageRating,
    String authorNickname,
    String authorEmail,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

  public static AdminGuidebookResponse from(Guidebook guidebook) {
    return AdminGuidebookResponse.builder()
        .id(guidebook.getId())
        .title(guidebook.getTitle())
        .description(guidebook.getDescription())
        .emoji(guidebook.getEmoji())
        .color(guidebook.getColor())
        .thumbnailUrl(guidebook.getThumbnailUrl())
        .isPublished(guidebook.getIsPublished())
        .publishedDate(guidebook.getPublishedDate())
        .participantCount(guidebook.getParticipantCount())
        .totalPlaceCount(guidebook.getTotalPlaceCount())
        .exp(guidebook.getExp())
        .ratingCount(guidebook.getRatingCount())
        .averageRating(guidebook.calculateAverageScore())
        .authorNickname(guidebook.getAuthor().getNickname())
        .authorEmail(guidebook.getAuthor().getEmail())
        .createdAt(guidebook.getCreatedAt())
        .updatedAt(guidebook.getUpdatedAt())
        .build();
  }
}
