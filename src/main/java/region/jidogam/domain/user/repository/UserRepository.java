package region.jidogam.domain.user.repository;


import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByNickname(String nickname);

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  Optional<User> findByNickname(String nickname);
}
