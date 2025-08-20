package region.jidogam.domain.stamp.Repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.stamp.entity.Stamp;

public interface StampRepository extends JpaRepository<Stamp, UUID> {

  // 유저의 마지막 도장 조회
  Optional<Stamp> findFirstByUser_IdOrderByCreatedAtDesc(UUID uuid);

  // 해당 장소에 도장을 찍었는지 확인
  boolean existsByUser_IdAndPlace_Id(UUID userId, UUID placeId);
}
