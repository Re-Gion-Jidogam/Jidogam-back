package region.jidogam.domain.user.dto;

import java.time.LocalDateTime;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.user.entity.User;

public record ParticipationGuidebookDto (

  Guidebook guidebook,
  User author,
  LocalDateTime lastStampedAt)
{
}
