package region.jidogam.domain.place.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import region.jidogam.domain.place.dto.PlaceSortBy;

@Component
public class StringToPlaceSortByConverter implements Converter<String, PlaceSortBy> {

  @Override
  public PlaceSortBy convert(String source) {
    return PlaceSortBy.from(source);
  }

}
