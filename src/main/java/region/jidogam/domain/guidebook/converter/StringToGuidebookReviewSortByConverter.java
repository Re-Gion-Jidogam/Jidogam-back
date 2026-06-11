package region.jidogam.domain.guidebook.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import region.jidogam.domain.guidebook.dto.GuidebookReviewSortBy;

@Component
public class StringToGuidebookReviewSortByConverter implements Converter<String, GuidebookReviewSortBy> {

  @Override
  public GuidebookReviewSortBy convert(String source) {
    return GuidebookReviewSortBy.from(source);
  }
}