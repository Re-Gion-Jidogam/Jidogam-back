package region.jidogam.domain.place.mapper;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import region.jidogam.common.util.DistanceCalculatorUtil;
import region.jidogam.domain.place.dto.PlaceResponse;
import region.jidogam.domain.place.entity.Place;

@Component
public class PlaceMapper {

  public PlaceResponse toResponse(Place place, Double userLat, Double userLon) {
    return toResponse(place, null, userLat, userLon);
  }

  public PlaceResponse toResponse(Place place, LocalDateTime visitedDate, Double userLat,
      Double userLon) {
    double y = place.getY().doubleValue();
    double x = place.getX().doubleValue();

    return PlaceResponse.builder()
        .pid(place.getId())
        .name(place.getName())
        .address(place.getAddress())
        .y(y)
        .x(x)
        .visitedDate(visitedDate)
        .guidebookCount(place.getGuidebookCount())
        .stampCount(place.getStampCount())
        .category(place.getCategory())
        .distanceInKm(DistanceCalculatorUtil.calculateDistance(userLat, userLon, y, x))
        .points(place.getPoints())
        .build();
  }
}
