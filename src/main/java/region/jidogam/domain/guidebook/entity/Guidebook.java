package region.jidogam.domain.guidebook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @OneToOne(mappedBy = "guidebook")
  private GuidebookAreaRatio areaRatio;

  @Column(nullable = false)
  private String title;

  @Column
  private String description;

  @Column
  private String thumbnailUrl;

  @Column
  private String mapImageUrl;

  @Column
  private String emoji;

  @Column
  private String color;

  @Column(nullable = false)
  @Builder.Default
  private Integer exp = 0;

  @Column(nullable = false)
  @Builder.Default
  private Long ratingSum = 0L;

  @Column(nullable = false)
  @Builder.Default
  private Integer ratingCount = 0;

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

  public void updateTitle(String title) {
    this.title = title;
  }

  public void updateDescription(String description) {
    this.description = description;
  }

  public void updateEmoji(String emoji) {
    this.emoji = emoji;
  }

  public void updateColor(String color) {
    this.color = color;
  }

  public void updateThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public void updateMapImageUrl(String mapImageUrl) {
    this.mapImageUrl = mapImageUrl;
  }

  public void increaseTotalPlaceCount() {
    this.totalPlaceCount += 1;
  }

  public void decreaseTotalPlaceCount() {
    this.totalPlaceCount -= 1;
  }

  public void publish() {
    this.isPublished = true;
    this.publishedDate = LocalDateTime.now();
  }

  public void unpublish() {
    this.isPublished = false;
    this.publishedDate = null;
  }

  public double calculateAverageScore() {
    if (this.getRatingCount() == 0) {
      return 0.0;
    }
    double average = (double) this.getRatingSum() / this.getRatingCount();
    return Math.round(average * 10.0) / 10.0;
  }

  public void invalidateAreaRatio() {
    this.areaRatio = null;
  }

  public void updateExp(int exp) {
    this.exp = exp;
  }
}
