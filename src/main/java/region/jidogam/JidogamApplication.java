package region.jidogam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class JidogamApplication {

  public static void main(String[] args) {
    SpringApplication.run(JidogamApplication.class, args);
  }

}
