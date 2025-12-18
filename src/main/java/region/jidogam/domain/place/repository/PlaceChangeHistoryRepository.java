package region.jidogam.domain.place.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.place.entity.PlaceChangeHistory;

public interface PlaceChangeHistoryRepository extends JpaRepository<PlaceChangeHistory, UUID> {

}
