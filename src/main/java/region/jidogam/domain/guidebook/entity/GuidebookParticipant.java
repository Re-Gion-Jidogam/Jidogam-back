package region.jidogam.domain.guidebook.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;
import region.jidogam.domain.user.entity.User;

@Entity
@Table(name = "guidebook_participants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "guidebook_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GuidebookParticipant extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "guidebook_id", nullable = false)
  private Guidebook guidebook;

}
