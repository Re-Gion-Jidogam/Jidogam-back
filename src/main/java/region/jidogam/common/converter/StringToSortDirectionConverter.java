package region.jidogam.common.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import region.jidogam.common.dto.SortDirection;

@Component
public class StringToSortDirectionConverter implements Converter<String, SortDirection> {

  @Override
  public SortDirection convert(String source) {
    return SortDirection.valueOf(source.toUpperCase());
  }
}
