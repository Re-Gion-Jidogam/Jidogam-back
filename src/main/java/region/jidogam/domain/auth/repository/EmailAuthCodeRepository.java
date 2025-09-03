package region.jidogam.domain.auth.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.auth.entity.EmailAuthCode;

public interface EmailAuthCodeRepository extends JpaRepository<EmailAuthCode, UUID> {

  Optional<EmailAuthCode> findByEmail(String email);

  Optional<EmailAuthCode> findFirstByEmailOrderByCreatedAtDesc(String email);
}
