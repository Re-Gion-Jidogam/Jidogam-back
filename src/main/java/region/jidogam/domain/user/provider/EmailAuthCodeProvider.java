package region.jidogam.domain.user.provider;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class EmailAuthCodeProvider {

  private static final int AUTH_CODE_BOUND = 1_000_000;
  private static final String AUTH_CODE_FORMAT = "%06d";

  public String generateAuthCode() {
    SecureRandom secureRandom = new SecureRandom();
    return String.format(AUTH_CODE_FORMAT, secureRandom.nextInt(AUTH_CODE_BOUND));
  }
}
