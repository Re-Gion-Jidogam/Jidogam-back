package region.jidogam.domain.guidebook.mapper;

import org.springframework.stereotype.Component;
import region.jidogam.domain.guidebook.dto.GuidebookReviewResponse;
import region.jidogam.domain.guidebook.dto.GuidebookReviewResponse.AuthorDto;
import region.jidogam.domain.guidebook.entity.GuidebookReview;
import region.jidogam.domain.user.entity.User;
import region.jidogam.domain.user.util.LevelCalculator;

@Component
public class GuidebookReviewMapper {

  public GuidebookReviewResponse toResponse(GuidebookReview review) {
    return GuidebookReviewResponse.builder()
        .rid(review.getId())
        .content(review.getContent())
        .rating(review.getRating())
        .createdAt(review.getCreatedAt())
        .updatedAt(review.getUpdatedAt())
        .author(toAuthorDto(review.getAuthor()))
        .build();
  }

  private AuthorDto toAuthorDto(User user) {
    if (user.isDeleted()) {
      return null;
    }
    return new AuthorDto(
        user.getId(),
        user.getNickname(),
        LevelCalculator.calculateLevel(user.getExp())
    );
  }
}