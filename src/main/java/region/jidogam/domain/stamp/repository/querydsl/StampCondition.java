package region.jidogam.domain.stamp.repository.querydsl;

import static org.springframework.util.StringUtils.hasText;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.UUID;
import region.jidogam.domain.stamp.entity.QStamp;

public class StampCondition {

  private static final QStamp stamp = QStamp.stamp;

  // 사용자 ID 조건
  public static BooleanExpression userIdEq(UUID userId) {
    return userId != null ? stamp.user.id.eq(userId) : null;
  }

  // 장소 이름 검색 조건
  public static BooleanExpression placeNameContains(String keyword) {
    return hasText(keyword) ? stamp.place.name.contains(keyword) : null;
  }
}
