package region.jidogam.domain.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import region.jidogam.domain.place.entity.Place;

public interface AdminPlaceRepository {

  Page<Place> searchPlaces(String keyword, Boolean deleted, Pageable pageable);
}
