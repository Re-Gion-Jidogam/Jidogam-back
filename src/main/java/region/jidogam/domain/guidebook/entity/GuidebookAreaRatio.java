package region.jidogam.domain.guidebook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;
import region.jidogam.domain.area.entity.Area;

@Entity
@Table(name = "guidebook_area_ratios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GuidebookAreaRatio extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private Guidebook guidebook;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "first_area_id", nullable = false)
  private Area firstArea;

  @Column
  private Double firstAreaRatio;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "second_area_id")
  private Area secondArea;

  @Column
  private Double secondAreaRatio;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "third_area_id")
  private Area thirdArea;

  @Column
  private Double thirdAreaRatio;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isPrimaryArea = false;

  public void setSecondArea(Area secondArea, Double secondAreaRatio) {
    this.secondArea = secondArea;
    this.secondAreaRatio = secondAreaRatio;
  }

  public void setThirdArea(Area thirdArea, Double thirdAreaRatio) {
    this.thirdArea = thirdArea;
    this.thirdAreaRatio = thirdAreaRatio;
  }
}
