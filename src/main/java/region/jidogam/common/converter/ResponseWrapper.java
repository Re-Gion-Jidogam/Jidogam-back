package region.jidogam.common.converter;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import region.jidogam.common.dto.response.ResponseDto;
import region.jidogam.common.exception.GlobalExceptionHandler;

@RestControllerAdvice(
  basePackages = "region.jidogam",
  basePackageClasses = GlobalExceptionHandler.class
)
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(
    MethodParameter returnType,
    Class<? extends HttpMessageConverter<?>> converterType
  ) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
    Object body,
    MethodParameter returnType,
    MediaType selectedContentType,
    Class<? extends HttpMessageConverter<?>> selectedConverterType,
    ServerHttpRequest request,
    ServerHttpResponse response
  ) {

    if (body instanceof ResponseDto) {
      return body;
    }

    return ResponseDto.ok(body);
  }
}
