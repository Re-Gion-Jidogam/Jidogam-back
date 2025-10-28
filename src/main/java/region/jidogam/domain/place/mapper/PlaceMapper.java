package region.jidogam.domain.place.mapper;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.entity.Place;

@Component
public class PlaceMapper {

  public PlaceResponse toResponse(Place place) {
    return toResponse(place, null);
  }

  public PlaceResponse toResponse(Place place, LocalDateTime visitedDate) {
    return PlaceResponse.builder()
        .pid(place.getId())
        .name(place.getName())
        .address(place.getAddress())
        .y(Double.parseDouble(place.getY().toString()))
        .x(Double.parseDouble(place.getX().toString()))
        .visitedDate(visitedDate)
        .guidebookCount(place.getGuidebookCount())
        .stampCount(place.getStampCount())
        .category(place.getCategory())
        .build();
  }
}
