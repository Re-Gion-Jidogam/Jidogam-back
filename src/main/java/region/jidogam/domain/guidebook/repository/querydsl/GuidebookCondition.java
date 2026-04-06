package region.jidogam.domain.guidebook.repository.querydsl;

import static org.springframework.util.StringUtils.hasText;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.LocalDateTime;
import java.util.UUID;
import region.jidogam.domain.guidebook.entity.QGuidebook;

/*
  조건 클래스: 동적 쿼리 - BooleanExpression 활용하기
 */
public class GuidebookCondition {

  private static final QGuidebook guidebook = QGuidebook.guidebook;

  // Id 조건
  public static BooleanExpression idLt(UUID id) {
    return id != null ? guidebook.id.lt(id) : null;
  }

  public static BooleanExpression idGt(UUID id) {
    return id != null ? guidebook.id.gt(id) : null;
  }

  // 제목 조건
  public static BooleanExpression titleContains(String title) {
    return hasText(title) ? guidebook.title.contains(title) : null;
  }

  // 출판 조건
  public static BooleanExpression isPublished() {
    return guidebook.isPublished.eq(true);
  }

  // 구독자 조건
  public static BooleanExpression participantCountLoe(Integer count) {
    return count != null ? guidebook.participantCount.loe(count) : null;
  }

  public static BooleanExpression participantCountGoe(Integer count) {
    return count != null ? guidebook.participantCount.goe(count) : null;
  }

  // 날짜 조건
  public static BooleanExpression createdAtLt(LocalDateTime dateTime) {
    return dateTime != null ? guidebook.createdAt.lt(dateTime) : null;
  }

  public static BooleanExpression createdAtGt(LocalDateTime dateTime) {
    return dateTime != null ? guidebook.createdAt.gt(dateTime) : null;
  }

  // local 조건
  public static BooleanExpression isLocalGuidebook(Boolean isLocal) {
    return isLocal != null ? guidebook.areaRatio.isPrimaryArea.eq(isLocal) : null;
  }

}
