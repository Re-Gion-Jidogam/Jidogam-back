package region.jidogam.domain.user.provider;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import region.jidogam.domain.auth.entity.EmailAuthCode;
import region.jidogam.domain.auth.exception.AlreadyUsedAuthCodeException;
import region.jidogam.domain.auth.exception.ExpiredEmailAuthException;
import region.jidogam.domain.auth.exception.InvalidEmailAuthException;

@Component
public class EmailAuthCodeProvider {

  private static final int AUTH_CODE_BOUND = 1_000_000;
  private static final String AUTH_CODE_FORMAT = "%06d";

  public String generateAuthCode() {
    SecureRandom secureRandom = new SecureRandom();
    return String.format(AUTH_CODE_FORMAT, secureRandom.nextInt(AUTH_CODE_BOUND));
  }

  //인증 코드 일치 여부 확인
  public void validateAuthCode(EmailAuthCode emailAuthCode, String authCode) {
    if (!emailAuthCode.getCode().equals(authCode)) {
      throw InvalidEmailAuthException.withCode(authCode);
    }
  }

  //인증 코드 만료 여부 확인
  public void validateNotExpired(EmailAuthCode emailAuthCode, String authCode) {
    if (emailAuthCode.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw ExpiredEmailAuthException.withCode(authCode);
    }
  }

  //인증 코드 사용 여부 확인
  public void validateNotUsed(EmailAuthCode emailAuthCode, String authCode) {
    if (emailAuthCode.getUsed()) {
      throw AlreadyUsedAuthCodeException.withCode(authCode);
    }
  }
}
