package region.jidogam.domain.guidebook.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import region.jidogam.domain.guidebook.dto.ParticipationFilter;

@Component
public class StringToParticipationFilterConverter implements
    Converter<String, ParticipationFilter> {

  @Override
  public ParticipationFilter convert(String source) {
    return ParticipationFilter.from(source);
  }
}
