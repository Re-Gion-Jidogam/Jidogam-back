package region.jidogam.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import region.jidogam.common.converter.MyHttpMessageConverter;
import region.jidogam.common.resolver.CurrentUserIdArgumentResolver;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final ObjectMapper objectMapper;

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    // 기존 MappingJackson2HttpMessageConverter를 찾아서 교체
    for (int i = 0; i < converters.size(); i++) {
      if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
        converters.set(i, new MyHttpMessageConverter(objectMapper));
        break;
      }
    }
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new CurrentUserIdArgumentResolver());
  }
}
