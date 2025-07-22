package region.jidogam.domain.stamp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import region.jidogam.common.entity.BaseEntity;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.user.entity.User;

@Entity
@Table(name = "stamps", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "place_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Stamp extends BaseEntity {

  @ManyToOne
  @JoinColumn(name ="user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name ="place_id", nullable = false)
  private Place place;

}
