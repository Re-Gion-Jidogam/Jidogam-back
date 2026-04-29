package region.jidogam.infrastructure.jwt;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByUserId(UUID userId);

  Optional<RefreshToken> findByRefreshToken(String refreshToken);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM RefreshToken r WHERE r.userId = :userId")
  int deleteByUserId(@Param("userId") UUID userId);
}
