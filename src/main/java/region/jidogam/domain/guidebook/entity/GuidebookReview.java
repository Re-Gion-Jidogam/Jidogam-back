package region.jidogam.domain.guidebook.entity;

import jakarta.persistence.Column;
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
import region.jidogam.common.entity.BaseUpdatableEntity;
import region.jidogam.domain.user.entity.User;

@Entity
@Table(name = "guidebook_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"author_id", "guidebook_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GuidebookReview extends BaseUpdatableEntity {

  @ManyToOne
  @JoinColumn(name = "guidebook_id", nullable = false)
  private Guidebook guidebook;

  @ManyToOne
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  @Builder.Default
  private Double rating = 0.0;

}
