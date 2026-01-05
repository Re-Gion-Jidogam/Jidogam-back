package region.jidogam.domain.area.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseUpdatableEntity;

@Entity
@Table(
    name = "areas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sido", "sigungu"})
}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Area extends BaseUpdatableEntity {

  @Column(nullable = false)
  private String sido;

  @Column(nullable = false)
  private String sigungu;

  @Enumerated(EnumType.STRING)
  private AreaType type;

  @Column(nullable = false)
  private Double weight;

  @Column(columnDefinition = "timestamp with time zone")
  private LocalDateTime weightUpdatedAt;

  @Column(nullable = false, unique = true, length = 10)
  private String sigunguCode;

  public enum AreaType {
    NORMAL,
    INTEREST,
    UNDERSERVED
  }

  public String areaName() {
    return sido + " " + sigungu;
  }

  public void updateWeight(double weight) {
    this.weight = weight;
    this.weightUpdatedAt = LocalDateTime.now();
  }

  public void updateType(AreaType type) {
    this.type = type;
  }
}
