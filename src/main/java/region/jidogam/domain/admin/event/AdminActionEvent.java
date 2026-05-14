package region.jidogam.domain.admin.event;

import java.util.Map;
import java.util.UUID;
import region.jidogam.domain.admin.dto.FieldChange;
import region.jidogam.domain.admin.entity.AdminActionHistory.ActionType;
import region.jidogam.domain.admin.entity.AdminActionHistory.TargetType;

public record AdminActionEvent(
    UUID adminId,
    ActionType actionType,
    TargetType targetType,
    UUID targetId,
    Map<String, FieldChange> changedFields,
    String description
) {

  public static AdminActionEvent of(
      UUID adminId,
      ActionType actionType,
      TargetType targetType,
      UUID targetId
  ) {
    return new AdminActionEvent(adminId, actionType, targetType, targetId, null, null);
  }

  public static AdminActionEvent of(
      UUID adminId,
      ActionType actionType,
      TargetType targetType,
      UUID targetId,
      Map<String, FieldChange> changedFields
  ) {
    return new AdminActionEvent(adminId, actionType, targetType, targetId, changedFields, null);
  }
}
