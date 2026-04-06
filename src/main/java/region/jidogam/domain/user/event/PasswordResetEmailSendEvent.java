package region.jidogam.domain.user.event;

import java.time.Duration;

public record PasswordResetEmailSendEvent(
    String email,
    String resetUrl,
    Duration expiration
) {
  public static PasswordResetEmailSendEvent of(String email, String resetUrl, Duration expiration) {
    return new PasswordResetEmailSendEvent(email, resetUrl, expiration);
  }
}
