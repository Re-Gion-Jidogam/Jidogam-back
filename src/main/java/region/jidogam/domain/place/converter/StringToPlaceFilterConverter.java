package region.jidogam.domain.place.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import region.jidogam.domain.place.dto.PlaceFilter;

@Component
public class StringToPlaceFilterConverter implements Converter<String, PlaceFilter> {

  @Override
  public PlaceFilter convert(String source) {
    return PlaceFilter.from(source);
  }
}
