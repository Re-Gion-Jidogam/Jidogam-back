package region.jidogam.common.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.common.entity.RetryFailureLog;

public interface RetryFailureLogRepository extends JpaRepository<RetryFailureLog, UUID> {

}
