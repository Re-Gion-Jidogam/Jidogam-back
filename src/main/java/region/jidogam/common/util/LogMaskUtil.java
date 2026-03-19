package region.jidogam.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogMaskUtil {

  /**
   * 이메일 주소를 마스킹합니다.
   * <p>
   * 예시: "user@example.com" → "us***@example.com"
   * 앞 2자 공개 + *** (고정), 도메인은 공개 정보이므로 그대로 노출
   */
  public static String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "***";
    }

    String[] parts = email.split("@");
    String local = parts[0];
    String domain = parts[1];

    String maskedLocal;
    if (local.length() <= 2) {
      maskedLocal = local.charAt(0) + "***";
    } else {
      maskedLocal = local.substring(0, 2) + "***";
    }

    return maskedLocal + "@" + domain;
  }
}
