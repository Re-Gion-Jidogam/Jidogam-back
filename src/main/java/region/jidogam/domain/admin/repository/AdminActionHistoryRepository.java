package region.jidogam.domain.admin.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.admin.entity.AdminActionHistory;

public interface AdminActionHistoryRepository extends JpaRepository<AdminActionHistory, UUID> {

}
