package region.jidogam.domain.user.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import region.jidogam.domain.user.dto.GuidebookParticipationSortBy;

@Component
public class StringToGuidebookParticipationSortByConverter implements
    Converter<String, GuidebookParticipationSortBy> {

  @Override
  public GuidebookParticipationSortBy convert(String source) {
    return GuidebookParticipationSortBy.from(source);
  }
}
