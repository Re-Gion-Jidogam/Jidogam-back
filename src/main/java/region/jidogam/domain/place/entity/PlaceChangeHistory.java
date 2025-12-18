package region.jidogam.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import region.jidogam.common.entity.BaseEntity;
import region.jidogam.domain.place.dto.FieldChange;

@Entity
@Table(name = "place_change_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PlaceChangeHistory extends BaseEntity {

  @Column(name = "place_id", nullable = false)
  private UUID placeId;

  @Column(name = "kakao_id", nullable = false)
  private String kakaoId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "JSONB", nullable = false)
  private Map<String, FieldChange> changedFields;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChangeSource source;

  public enum ChangeSource {
    KAKAO_API,      // 카카오 API에서 감지된 변경
    USER_REPORT,    // 사용자 제보
    ADMIN           // 관리자 수정
  }
}
