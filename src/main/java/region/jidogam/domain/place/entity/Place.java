package region.jidogam.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;
import region.jidogam.domain.area.entity.Area;

@Entity
@Table(
    name = "places", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"source", "external_id"})
}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Place extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "area_id", nullable = false)
  private Area area;

  @Column(nullable = false)
  private String externalId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Source source;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private BigDecimal x;

  @Column(nullable = false)
  private BigDecimal y;

  @Column(nullable = false)
  private String jibunAddress;

  @Column
  private String roadAddress;

  @Column(nullable = false)
  private LocalDateTime fetchedAt;

  @Column
  private String categoryCode;

  @Column
  private String categoryName;

  @Column(nullable = false)
  @Builder.Default
  private Integer exp = 0;

  @Column
  @Builder.Default
  private Integer guidebookCount = 0;

  @Column
  @Builder.Default
  private Integer stampCount = 0;

  public void updateName(String name) {
    this.name = name;
  }

  public void updateAddress(String jibunAddress, String roadAddress) {
    this.jibunAddress = jibunAddress;
    this.roadAddress = roadAddress;
  }

  public void updateCoordinates(BigDecimal x, BigDecimal y) {
    this.x = x;
    this.y = y;
  }

  public void updateFetchedAt(LocalDateTime fetchedAt) {
    this.fetchedAt = fetchedAt;
  }

  public void updateCategory(String categoryCode, String categoryName) {
    this.categoryCode = categoryCode;
    this.categoryName = categoryName;
  }

  public void updateArea(Area area) {
    this.area = area;
  }

  public void updateExp(int exp) {
    this.exp = exp;
  }

  /**
   * 장소 데이터의 출처
   */
  public enum Source {
    SEMAS
  }
}
