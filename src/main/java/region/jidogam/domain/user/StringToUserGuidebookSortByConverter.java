package region.jidogam.domain.user;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import region.jidogam.domain.user.dto.UserGuideBookSortBy;

@Component
public class StringToUserGuidebookSortByConverter implements Converter<String, UserGuideBookSortBy> {

  @Override
  public UserGuideBookSortBy convert(String source) {
    return UserGuideBookSortBy.from(source);
  }
}
