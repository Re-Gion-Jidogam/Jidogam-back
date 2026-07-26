package region.jidogam.domain.area.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;

@Entity
@Table(
    name = "area_legacy_codes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"legacy_code"})
}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AreaLegacyCode extends BaseEntity {

  @Column(nullable = false, length = 10)
  private String legacyCode;

  @Column(length = 50)
  private String legacyName;

  @Column
  private LocalDate effectiveFrom;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "area_id", nullable = false)
  private Area area;
}
