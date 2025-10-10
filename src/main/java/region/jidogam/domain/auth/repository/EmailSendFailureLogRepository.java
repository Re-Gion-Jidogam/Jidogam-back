package region.jidogam.domain.auth.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.auth.entity.EmailSendFailureLog;

public interface EmailSendFailureLogRepository extends JpaRepository<EmailSendFailureLog, UUID> {

}
