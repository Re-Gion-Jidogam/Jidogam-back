package region.jidogam.domain.guidebook.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.guidebook.entity.Guidebook;
import region.jidogam.domain.guidebook.entity.GuidebookPlace;
import region.jidogam.domain.place.entity.Place;

public interface GuidebookPlaceRepository extends JpaRepository<GuidebookPlace, UUID> {

  void deleteByGuidebookAndPlace(Guidebook guidebook, Place place);

  void deleteByGuidebook(Guidebook guidebook);
}
