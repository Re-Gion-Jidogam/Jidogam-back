package region.jidogam.infrastructure.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import region.jidogam.common.dto.response.ResponseDto;
import region.jidogam.common.util.CookieUtil;
import region.jidogam.domain.auth.exception.AuthErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;
  private final CookieUtil cookieUtil;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {

    log.warn("인증 오류: {}, path = {}", authException.getMessage(), request.getRequestURI());

    AuthErrorCode errorCode = AuthErrorCode.UNAUTHORIZED;

    ResponseDto<Void> responseDto = ResponseDto.error(errorCode.getCode(), errorCode.getMessage());

    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // refresh token 쿠키 무효화
    response.addHeader("Set-Cookie", cookieUtil.deleteRefreshTokenCookie().toString());

    String jsonResponse = objectMapper.writeValueAsString(responseDto);
    response.getWriter().write(jsonResponse);
  }
}
