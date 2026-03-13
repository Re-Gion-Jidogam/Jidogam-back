package region.jidogam.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogMaskUtil {

  /**
   * 이메일 주소를 마스킹합니다.
   * <p>
   * 예시: "user@example.com" → "u***@e***.com"
   */
  public static String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "***";
    }

    String[] parts = email.split("@");
    String local = parts[0];
    String domain = parts[1];

    String maskedLocal = local.charAt(0) + "***";

    int dotIndex = domain.lastIndexOf('.');
    if (dotIndex <= 0) {
      return maskedLocal + "@***";
    }

    String maskedDomain = domain.charAt(0) + "***" + domain.substring(dotIndex);
    return maskedLocal + "@" + maskedDomain;
  }
}
