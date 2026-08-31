package region.jidogam.domain.area.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
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
    @UniqueConstraint(columnNames = {"parent_id", "code"})
}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Area extends BaseUpdatableEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, length = 10)
  private String code;

  @Enumerated(EnumType.STRING)
  private PopulationDeclineCategory populationDeclineCategory;

  @Enumerated(EnumType.STRING)
  private AdministrativeLevel administrativeLevel;

  @Column
  private Double weight;

  @Column(columnDefinition = "timestamp with time zone")
  private LocalDateTime weightUpdatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  private Area parent;

  /**
   * 시군구 단위 소외지역 분류
   */
  public enum PopulationDeclineCategory {
    NORMAL,
    INTEREST,
    UNDERSERVED
  }

  public void updateWeight(double weight) {
    this.weight = weight;
    this.weightUpdatedAt = LocalDateTime.now();
  }

  public void updatePopulationDeclineCategory(PopulationDeclineCategory populationDeclineCategory) {
    this.populationDeclineCategory = populationDeclineCategory;
  }
}
