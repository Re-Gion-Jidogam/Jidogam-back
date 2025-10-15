package region.jidogam.domain.guidebook.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import region.jidogam.domain.guidebook.dto.GuidebookFilter;

@Component
public class StringToGuidebookFilterConverter implements Converter<String, GuidebookFilter> {

  @Override
  public GuidebookFilter convert(String source) {
    return GuidebookFilter.from(source);
  }
}
