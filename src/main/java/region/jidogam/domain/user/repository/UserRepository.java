package region.jidogam.domain.user.repository;


import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import region.jidogam.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByNickname(String nickname);

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  Optional<User> findByNickname(String nickname);

  /**
   * 유저 조회 시 행 락을 획득하며, 락을 기다리지 않고 바로 실패
   * (PostgreSQL: SELECT ... FOR NO KEY UPDATE NOWAIT)
   *
   * @param id 유저 ID
   * @return 유저
   * @throws org.springframework.dao.PessimisticLockingFailureException 다른 트랜잭션이 이미 락을 잡고 있는 경우
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
  @Query("SELECT u FROM User u WHERE u.id = :id")
  Optional<User> findByIdForUpdateNoWait(UUID id);
}
