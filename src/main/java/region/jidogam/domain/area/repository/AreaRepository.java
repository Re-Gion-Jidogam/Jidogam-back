package region.jidogam.domain.area.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.area.entity.Area;

public interface AreaRepository extends JpaRepository<Area, UUID> {

  boolean existsBySigunguCode(String sigunguCode);
}
