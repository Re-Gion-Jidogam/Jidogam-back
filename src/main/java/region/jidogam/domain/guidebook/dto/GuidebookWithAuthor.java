package region.jidogam.domain.guidebook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.user.entity.User;

@AllArgsConstructor
@Getter
@Setter
public class GuidebookWithAuthor {
  private Guidebook guidebook;
  private User user; // Author
}