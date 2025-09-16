package region.jidogam.infrastructure.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import region.jidogam.common.dto.response.ResponseDto;
import region.jidogam.domain.auth.exception.AuthErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {

    log.warn("Access denied: {}, path: {}", accessDeniedException.getMessage(), request.getRequestURI());

    AuthErrorCode errorCode = AuthErrorCode.ACCESS_DENIED;

    ResponseDto<Void> responseDto = ResponseDto.error(errorCode.getCode(), errorCode.getMessage());

    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    String jsonResponse = objectMapper.writeValueAsString(responseDto);
    response.getWriter().write(jsonResponse);
  }
}
