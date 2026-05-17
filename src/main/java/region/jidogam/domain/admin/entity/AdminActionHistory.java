package region.jidogam.domain.admin.entity;

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
import region.jidogam.domain.admin.dto.FieldChange;

@Entity
@Table(name = "admin_actions_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AdminActionHistory extends BaseEntity {

  @Column(name = "admin_id", nullable = false)
  private UUID adminId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_type", nullable = false)
  private ActionType actionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false)
  private TargetType targetType;

  @Column(name = "target_id")
  private UUID targetId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "changed_fields", columnDefinition = "JSONB")
  private Map<String, FieldChange> changedFields;

  @Column(length = 500)
  private String description;

  public enum ActionType {
    LOGIN,
    CREATE,
    UPDATE,
    DELETE,
    RESTORE
  }

  public enum TargetType {
    ADMIN,
    USER
  }
}
