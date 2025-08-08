package region.jidogam.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {

    // HTTP 클라이언트 설정
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    
    // 연결 타임아웃
    factory.setConnectTimeout(3000); // 3초

    // 읽기 타임아웃
    factory.setReadTimeout(5000); // 5초

    return new RestTemplate();
  }
}
