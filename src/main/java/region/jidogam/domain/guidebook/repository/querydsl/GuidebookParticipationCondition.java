package region.jidogam.domain.guidebook.repository.querydsl;

import static region.jidogam.domain.guidebook.entity.QGuidebook.guidebook;
import static region.jidogam.domain.guidebook.entity.QGuidebookParticipation.guidebookParticipation;

import com.querydsl.core.types.dsl.BooleanExpression;
import region.jidogam.domain.guidebook.dto.ParticipationFilter;

public class GuidebookParticipationCondition {

  /**
   * 가이드북 제목 검색 조건
   */
  public static BooleanExpression titleContains(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }
    return guidebook.title.containsIgnoreCase(keyword);
  }

  /**
   * 완료 여부 필터 조건
   */
  public static BooleanExpression isCompletedFilter(ParticipationFilter filter) {
    if (filter == null) {
      return null;
    }

    return switch (filter) {
      case COMPLETED -> guidebookParticipation.isCompleted.isTrue();
      case PROGRESS -> guidebookParticipation.isCompleted.isFalse();
    };
  }
}
