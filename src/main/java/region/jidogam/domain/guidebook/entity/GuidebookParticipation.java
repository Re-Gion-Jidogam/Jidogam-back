package region.jidogam.domain.guidebook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;
import region.jidogam.domain.user.entity.User;

@Entity
@Table(name = "guidebook_participations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "guidebook_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GuidebookParticipation extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "guidebook_id", nullable = false)
  private Guidebook guidebook;

  @Column
  private LocalDateTime lastActivityAt;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isCompleted = false;

  @Column
  private LocalDateTime completedAt;

  @Column(nullable = false)
  private Integer completedPlaceCount = 0;

  @Builder.Default
  @Column(nullable = false)
  private Integer earnedExp = 0;

  public void updateLastActivityAt(LocalDateTime lastActivityAt) {
    this.lastActivityAt = lastActivityAt;
  }

  public void markAsCompleted(LocalDateTime completedAt) {
    this.isCompleted = true;
    this.completedAt = completedAt;
  }

  public void markAsInProgress() {
    this.isCompleted = false;
  }

}
