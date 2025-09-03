package region.jidogam.domain.guidebook.mapper;

import org.springframework.stereotype.Component;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.dto.GuidebookResponse.AuthorDto;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.user.entity.User;

@Component
public class GuidebookMapper {

  public GuidebookResponse toResponse(Guidebook guidebook) {
    return toResponse(guidebook, 0);
  }

  public GuidebookResponse toResponse(Guidebook guidebook, int visitedPlaceCount) {
    return GuidebookResponse.builder()
      .gid(guidebook.getId())
      .title(guidebook.getTitle())
      .description(guidebook.getDescription())
      .thumbnailUrl(guidebook.getThumbnailUrl())
      .emoji(guidebook.getEmoji())
      .color(guidebook.getColor())
      .point(guidebook.getPoints())
      .createdAt(guidebook.getCreatedAt())
      .updatedAt(guidebook.getUpdatedAt())
      .publishedDate(guidebook.getPublishedDate())
      .mapImageUrl(guidebook.getMapImageUrl())
      .score(guidebook.calculateAverageScore())
      .participantCount(guidebook.getParticipantCount())
      .totalPlaceCount(guidebook.getTotalPlaceCount())
      .visitedPlaceCount(visitedPlaceCount)
      .author(toAuthorDto(guidebook.getAuthor()))
      .build();
  }

  private AuthorDto toAuthorDto(User user) {
    return new GuidebookResponse.AuthorDto(
      user.getId(),
      user.getNickname(),
      null // 임시 처리
    );
  }
}
