package region.jidogam.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

@Builder
public record ResponseDto<T>(
    String code,
    T data,
    String message
) {
  public static <T> ResponseDto<T> ok(T data) {
    return ResponseDto.<T>builder()
        .code(null)
        .data(data)
        .message(null)
        .build();
  }

  public static <Void> ResponseDto<Void> error(String code, String message) {
    return ResponseDto.<Void>builder()
        .code(code)
        .data(null)// TODO 필요하다 하시면 넣기
        .message(message)
        .build();

  }
}

