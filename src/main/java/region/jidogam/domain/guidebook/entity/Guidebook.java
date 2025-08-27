package region.jidogam.domain.guidebook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseUpdatableEntity;
import region.jidogam.domain.user.entity.User;

@Entity
@Table(name = "guidebooks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Guidebook extends BaseUpdatableEntity {

  @ManyToOne
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @Column(nullable = false)
  private String title;

  @Column
  private String description;

  @Column
  private String thumbnailUrl;

  @Column
  private String emoji;

  @Column
  private String color;

  @Column(nullable = false)
  @Builder.Default
  private Integer points = 0;

  @Column(nullable = false)
  @Builder.Default
  private Double score = 0.0;

  @Column(nullable = false)
  @Builder.Default
  private Integer participantCount = 0;

  @Column(nullable = false)
  @Builder.Default
  private Integer totalPlaceCount = 0;

  @Column
  @Builder.Default
  private Boolean isPublished = false;

  @Column
  private LocalDateTime publishedDate;

}
