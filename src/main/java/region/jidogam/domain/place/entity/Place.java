package region.jidogam.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;
import region.jidogam.domain.area.entity.Area;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Place extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "area_id", nullable = false)
  private Area area;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private BigDecimal x;

  @Column(nullable = false)
  private BigDecimal y;

  @Column(nullable = false)
  private String address;

  @Column
  private String category;

  @Column(nullable = false)
  @Builder.Default
  private Integer points = 0;
}
