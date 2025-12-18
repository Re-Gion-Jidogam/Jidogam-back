package region.jidogam.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "area_id", nullable = false)
  private Area area;

  @Column(nullable = false, unique = true)
  private String kakaoId;

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

  @Column
  @Builder.Default
  private Integer guidebookCount = 0;

  @Column
  @Builder.Default
  private Integer stampCount = 0;

  public void updateName(String name) {
    this.name = name;
  }

  public void updateAddress(String address) {
    this.address = address;
  }

  public void updateCoordinates(BigDecimal x, BigDecimal y) {
    this.x = x;
    this.y = y;
  }

  public void updateCategory(String category) {
    this.category = category;
  }

  public void updateArea(Area area) {
    this.area = area;
  }

  public void updatePoint(int points) {
    this.points = points;
  }
}
