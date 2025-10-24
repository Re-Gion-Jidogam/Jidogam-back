package region.jidogam.domain.guidebook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;

@Entity
@Table(name = "guidebook_area_ratios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GuidebookAreaRatio extends BaseEntity {

  @Column(nullable = false)
  private UUID guidebookId;

  @Column(nullable = false)
  private UUID firstAreaId;

  @Column
  private Double firstAreaRatio;

  @Column
  private UUID secondAreaId;

  @Column
  private Double secondAreaRatio;

  @Column
  private UUID thirdAreaId;

  @Column
  private Double thirdAreaRatio;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isPrimaryArea = false;

  public void setSecondArea(UUID secondAreaId, Double secondAreaRatio) {
    this.secondAreaId = secondAreaId;
    this.secondAreaRatio = secondAreaRatio;
  }

  public void setThirdArea(UUID thirdAreaId, Double thirdAreaRatio) {
    this.thirdAreaId = thirdAreaId;
    this.thirdAreaRatio = thirdAreaRatio;
  }
}
