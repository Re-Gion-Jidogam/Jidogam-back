package region.jidogam.domain.guidebook.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookParticipation;
import region.jidogam.domain.guidebook.repository.querydsl.GuidebookParticipationRepositoryCustom;
import region.jidogam.domain.user.entity.User;

public interface GuidebookParticipationRepository extends
    JpaRepository<GuidebookParticipation, UUID>,
    GuidebookParticipationRepositoryCustom {

  boolean existsByGuidebookAndUser(Guidebook guidebook, User user);

  int deleteByGuidebook_IdAndUser_Id(UUID guidebookId, UUID userId);

  void deleteByGuidebook_Id(UUID guidebookId);

  /**
   * 가이드북 참여 목록 조회
   *
   * @param userId 유저 ID
   * @return 가이드북 참여 목록
   */
  @Query("""
        SELECT gp FROM GuidebookParticipation gp
        JOIN FETCH gp.guidebook g
        WHERE gp.user.id = :userId
      """)
  List<GuidebookParticipation> findByUserId(@Param("userId") UUID userId);

  /**
   * 유저가 진행 중인 가이드북 참여 목록 조회
   * (완료되지 않은 가이드북만 조회)
   *
   * @param userId 유저 ID
   * @return 진행 중인 가이드북 참여 목록
   */
  @Query("""
        SELECT gp FROM GuidebookParticipation gp
        JOIN FETCH gp.guidebook g
        WHERE gp.user.id = :userId
        AND gp.isCompleted = false
      """)
  List<GuidebookParticipation> findInProgressByUserId(@Param("userId") UUID userId);
}
