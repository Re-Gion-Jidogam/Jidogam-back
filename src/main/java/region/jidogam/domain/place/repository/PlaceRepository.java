package region.jidogam.domain.place.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import region.jidogam.domain.place.entity.Place;
import region.jidogam.domain.place.repository.querydsl.PlaceRepositoryCustom;

public interface PlaceRepository extends JpaRepository<Place, UUID>, PlaceRepositoryCustom {

  List<Place> findAllByOrderByStampCountDesc(Pageable pageable);
}
