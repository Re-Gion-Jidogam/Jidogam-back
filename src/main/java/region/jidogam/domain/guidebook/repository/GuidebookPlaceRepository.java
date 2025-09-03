package region.jidogam.domain.guidebook.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.guidebook.entity.GuidebookPlace;

public interface GuidebookPlaceRepository extends JpaRepository<GuidebookPlace, UUID> {

}
