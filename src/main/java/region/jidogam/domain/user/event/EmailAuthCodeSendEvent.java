package region.jidogam.domain.user.event;

import java.time.Duration;

public record EmailAuthCodeSendEvent(
    String email,
    String authCode,
    Duration expiration
) {
  public static EmailAuthCodeSendEvent of(String email, String authCode, Duration expiration) {
    return new EmailAuthCodeSendEvent(email, authCode, expiration);
  }
}
