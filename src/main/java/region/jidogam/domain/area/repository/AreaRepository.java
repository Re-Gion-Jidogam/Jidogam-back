package region.jidogam.domain.area.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.area.entity.Area;

public interface AreaRepository extends JpaRepository<Area, UUID> {

  boolean existsByParentIsNullAndCode(String code);

  boolean existsByParent_IdAndCode(UUID parentId, String code);

  Optional<Area> findByCode(String code);
}
