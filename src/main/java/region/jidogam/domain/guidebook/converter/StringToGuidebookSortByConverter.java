package region.jidogam.domain.guidebook.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import region.jidogam.domain.guidebook.dto.GuidebookSortBy;

@Component
public class StringToGuidebookSortByConverter implements Converter<String, GuidebookSortBy> {

  @Override
  public GuidebookSortBy convert(String source) {
    return GuidebookSortBy.from(source);
  }
}
