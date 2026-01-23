package region.jidogam.domain.guidebook.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import region.jidogam.domain.File.storage.FileStorage;
import region.jidogam.domain.guidebook.dto.GuidebookResponse;
import region.jidogam.domain.guidebook.dto.GuidebookResponse.AreaRatioDto;
import region.jidogam.domain.guidebook.dto.GuidebookResponse.AuthorDto;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookAreaRatio;
import region.jidogam.domain.user.entity.User;

@Component
@RequiredArgsConstructor
public class GuidebookMapper {

  private final FileStorage fileStorage;

  public GuidebookResponse toResponse(Guidebook guidebook) {
    return toResponse(guidebook, 0);
  }

  public GuidebookResponse toResponse(Guidebook guidebook, int visitedPlaceCount) {
    return GuidebookResponse.builder()
        .gid(guidebook.getId())
        .title(guidebook.getTitle())
        .description(guidebook.getDescription())
        .thumbnailUrl(fileStorage.generateGetUrl(guidebook.getThumbnailUrl()))
        .emoji(guidebook.getEmoji())
        .color(guidebook.getColor())
        .exp(guidebook.getExp())
        .createdAt(guidebook.getCreatedAt())
        .updatedAt(guidebook.getUpdatedAt())
        .publishedDate(guidebook.getPublishedDate())
        .mapImageUrl(fileStorage.generateGetUrl(guidebook.getMapImageUrl()))
        .score(guidebook.calculateAverageScore())
        .participantCount(guidebook.getParticipantCount())
        .totalPlaceCount(guidebook.getTotalPlaceCount())
        .visitedPlaceCount(visitedPlaceCount)
        .author(toAuthorDto(guidebook.getAuthor()))
        .areaRatio(toAreaRatioDto(guidebook.getAreaRatio()))
        .build();
  }

  private AuthorDto toAuthorDto(User user) {
    if (user.isDeleted()) {
      return null;
    }
    return new GuidebookResponse.AuthorDto(
        user.getId(),
        user.getNickname(),
        null // 임시 처리
    );
  }

  private AreaRatioDto toAreaRatioDto(GuidebookAreaRatio guidebookAreaRatio) {
    if (guidebookAreaRatio == null) {
      return null;
    }

    return new AreaRatioDto(
        guidebookAreaRatio.getFirstArea().areaName(),
        guidebookAreaRatio.getFirstAreaRatio(),
        guidebookAreaRatio.getSecondArea() != null
            ? guidebookAreaRatio.getSecondArea().areaName() : null,
        guidebookAreaRatio.getSecondArea() != null
            ? guidebookAreaRatio.getSecondAreaRatio() : null,
        guidebookAreaRatio.getThirdArea() != null
            ? guidebookAreaRatio.getThirdArea().areaName() : null,
        guidebookAreaRatio.getThirdArea() != null
            ? guidebookAreaRatio.getThirdAreaRatio() : null,
        guidebookAreaRatio.getIsPrimaryArea()
    );

  }
}
