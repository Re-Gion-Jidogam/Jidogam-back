package region.jidogam.domain.area.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;
import region.jidogam.common.entity.BaseUpdatableEntity;

@Entity
@Table(name = "areas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Area extends BaseUpdatableEntity {

  @Column(nullable = false)
  private String sido;

  @Column(nullable = false)
  private String sigungu;

  @Column(nullable = false)
  private Integer weight;

}
