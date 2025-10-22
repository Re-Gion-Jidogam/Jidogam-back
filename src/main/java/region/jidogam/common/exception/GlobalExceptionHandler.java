package region.jidogam.common.exception;

import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import region.jidogam.common.dto.response.ResponseDto;
import region.jidogam.common.util.CookieUtil;
import region.jidogam.domain.auth.exception.AuthErrorCode;
import region.jidogam.infrastructure.security.JidogamUserDetails;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final CookieUtil cookieUtil;

  // validation
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseDto<Void>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {

    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;
    FieldError fieldError = ex.getFieldErrors().get(0);

    log.info("Validation failed: {} - {}", fieldError.getField(),
        fieldError.getRejectedValue());

    String errMsg = "'" + fieldError.getField() + "=" + fieldError.getRejectedValue() + "' "
        + fieldError.getDefaultMessage();

    return createErrorResponse(errorCode, errMsg);
  }

  // request method
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<ResponseDto<Void>> handleHttpRequestMethodNotSupport(
      HttpRequestMethodNotSupportedException ex) {

    log.info("Request method not supported: {}", ex.getMethod());

    ErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;

    return createErrorResponse(errorCode, ex.getMessage());
  }

  // resource : 잘못된 uri
  @ExceptionHandler(NoResourceFoundException.class)
  protected ResponseEntity<ResponseDto<Void>> handleNoResourceFoundException(
      NoResourceFoundException ex) {

    log.info("Request for unsupported URI: {}", ex.getResourcePath());

    ErrorCode errorCode = CommonErrorCode.URI_NOT_FOUND;

    return createErrorResponse(errorCode, ex.getMessage());
  }

  // 잘못된 입력 값 : 타입 불일치 등의 json 파싱 실패
  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<ResponseDto<Void>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {

    log.info("Request with invalid JSON format: {} | Error: {}",
        ex.getHttpInputMessage(), ex.getMostSpecificCause().getMessage());

    ErrorCode errorCode = CommonErrorCode.INVALID_JSON_FORMAT;

    return createErrorResponse(errorCode, ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  protected ResponseEntity<ResponseDto<Void>> handleIllegalArgumentException(
      IllegalArgumentException ex
  ) {

    log.info("Request with invalid input: {}", ex.getMessage());

    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;

    return createErrorResponse(errorCode, ex.getMessage());
  }

  // business
  @ExceptionHandler(JidogamException.class)
  protected ResponseEntity<ResponseDto<Void>> handleBusinessException(JidogamException ex) {

    ErrorCode errorCode = ex.getErrorCode();

    return createErrorResponse(errorCode, ex.getMessage());
  }

  // other
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ResponseDto<Void>> handleUnexpectedException(Exception ex) {

    log.error("Exception", ex);

    ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

    return createErrorResponse(errorCode, errorCode.getMessage());
  }

  // method level security
  @ExceptionHandler(AuthorizationDeniedException.class)
  protected ResponseEntity<ResponseDto<Void>> handleAuthorizationDeniedException(
      AuthorizationDeniedException ex,
      HttpServletRequest request,
      @AuthenticationPrincipal JidogamUserDetails principal
  ) {
    log.warn("Authorization denied: {}, path: {}, userId: {}, role: {}",
        ex.getMessage(),
        request.getRequestURI(),
        principal != null ? principal.getId() : "anonymous",
        principal != null ? principal.getAuthorities() : "anonymous"
    );

    AuthErrorCode errorCode = AuthErrorCode.ACCESS_DENIED;

    return createErrorResponse(errorCode, ex.getMessage());
  }

  // auth
  @ExceptionHandler(AuthException.class)
  protected ResponseEntity<ResponseDto<Void>> handleAuthException(AuthException ex,
      HttpServletResponse response) {

    ErrorCode errorCode = AuthErrorCode.UNAUTHORIZED;

    log.info("Authentication failed : {} | Error: {})",
        errorCode, errorCode.getMessage());

    // refresh token 쿠키 무효화
    ResponseCookie refreshCookie = cookieUtil.deleteRefreshTokenCookie();
    response.addHeader("Set-Cookie", refreshCookie.toString());

    return createErrorResponse(errorCode, ex.getMessage());
  }

  // 응답
  private ResponseEntity<ResponseDto<Void>> createErrorResponse(ErrorCode errorCode,
      String message) {
    ResponseDto<Void> responseDto = ResponseDto.error(errorCode.getCode(), message);
    return ResponseEntity
        .status(errorCode.getStatus())
        .contentType(MediaType.APPLICATION_JSON)
        .body(responseDto);
  }
}
