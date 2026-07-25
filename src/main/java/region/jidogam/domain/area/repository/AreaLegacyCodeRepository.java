package region.jidogam.domain.area.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.area.entity.AreaLegacyCode;

public interface AreaLegacyCodeRepository extends JpaRepository<AreaLegacyCode, UUID> {

  Optional<AreaLegacyCode> findByLegacyCode(String legacyCode);

  boolean existsByLegacyCode(String legacyCode);
}
